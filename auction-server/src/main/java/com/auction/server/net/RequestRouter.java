package com.auction.server.net;

import com.auction.common.dto.AuctionStatusDTO;
import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.BidRequest;
import com.auction.common.dto.Request;
import com.auction.common.dto.Response;
import com.auction.server.service.AuctionManager;

import java.util.List;
import java.util.Objects;

public class RequestRouter {
    private final AuctionManager auctionManager;

    public RequestRouter(AuctionManager auctionManager) {
        this.auctionManager = Objects.requireNonNull(auctionManager, "AuctionManager must not be null.");
    }

    public Response route(Request request, ClientHandler clientHandler) {
        if (request == null || request.getCommand() == null) {
            return Response.error("Request hoặc Command không được null.");
        }

        try {
            return switch (request.getCommand()) {
                case LIST_AUCTIONS -> {
                    List<AuctionStatusDTO> list = auctionManager.listAuctionStatuses();
                    yield Response.ok("Danh sách phiên đấu giá", list);
                }
                case GET_AUCTION_DETAIL -> {
                    String auctionId = (String) request.getPayload();
                    AuctionStatusDTO status = auctionManager.getAuctionStatus(auctionId);
                    yield Response.ok("Chi tiết phiên đấu giá", status);
                }
                case PLACE_BID -> {
                    BidRequest bidReq = (BidRequest) request.getPayload();
                    AuctionStatusDTO updated = auctionManager.placeBid(bidReq);
                    yield Response.ok("Đặt giá thành công", updated);
                }
                case SET_AUTOBID -> {
                    AutoBidRequest autoReq = (AutoBidRequest) request.getPayload();
                    AuctionStatusDTO updated = auctionManager.registerAutoBid(autoReq);
                    yield Response.ok("Đăng ký Auto-Bid thành công", updated);
                }
                case WATCH_AUCTION -> {
                    String auctionId = (String) request.getPayload();
                    if (clientHandler != null) {
                        auctionManager.getEventPublisher().subscribe(auctionId, clientHandler.getObserver());
                    }
                    AuctionStatusDTO current = auctionManager.getAuctionStatus(auctionId);
                    yield Response.ok("Đã theo dõi phiên đấu giá", current);
                }
                case UNWATCH_AUCTION -> {
                    String auctionId = (String) request.getPayload();
                    if (clientHandler != null) {
                        auctionManager.getEventPublisher().unsubscribe(auctionId, clientHandler.getObserver());
                    }
                    yield Response.ok("Đã hủy theo dõi phiên đấu giá", null);
                }
                default -> Response.error("Lệnh chưa được hỗ trợ hoặc đang chờ tích hợp AuthService: " + request.getCommand());
            };
        } catch (IllegalArgumentException | IllegalStateException | ClassCastException e) {
            return Response.error(e.getMessage());
        } catch (Exception e) {
            return Response.error("Lỗi hệ thống: " + e.getMessage());
        }
    }

    public void onClientDisconnected(ClientHandler clientHandler) {
        if (clientHandler != null) {
            auctionManager.getEventPublisher().unsubscribeAll(clientHandler.getObserver());
        }
    }
}