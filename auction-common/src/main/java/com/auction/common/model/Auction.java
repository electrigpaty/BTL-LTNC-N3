package com.auction.common.model;

import com.auction.common.dto.AuctionStatusDTO;
import com.auction.common.enums.AuctionState;
import com.auction.common.model.item.Item;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Auction extends Entity {
    private static final long serialVersionUID = 1L;

    private final Item item;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionState state;
    private double currentPrice;
    private String leadingBidderId; // null nếu chưa ai đặt giá
    private final List<BidTransaction> bidHistory = new ArrayList<>();

    public Auction(Item item, LocalDateTime startTime, LocalDateTime endTime) {
        this(null, item, startTime, endTime, AuctionState.OPEN, List.of());
    }

    public Auction(String id, Item item, LocalDateTime startTime, LocalDateTime endTime,
                   AuctionState state, List<BidTransaction> bids) {
        super(id);
        if (item == null) {
            throw new IllegalArgumentException("Sản phẩm của phiên đấu giá không được null.");
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
        this.item = item;
        this.startTime = startTime;
        this.endTime = endTime;
        this.state = Objects.requireNonNull(state, "Trạng thái không được null.");
        this.currentPrice = item.getStartingPrice();
        for (BidTransaction bid : Objects.requireNonNull(bids, "Danh sách bid không được null.")) {
            bidHistory.add(bid);
            if (leadingBidderId == null || bid.getAmount() > currentPrice) {
                currentPrice = bid.getAmount();
                leadingBidderId = bid.getBidderId();
            }
        }
    }

    public synchronized BidTransaction placeBid(String bidderId, double amount, boolean autoBid,
                                                LocalDateTime now) {
        if (bidderId == null || bidderId.isBlank()) {
            throw new IllegalArgumentException("Mã người đặt giá không được để trống.");
        }
        Objects.requireNonNull(now, "Thời điểm đặt giá không được null.");
        if (!state.acceptsBids()) {
            throw new IllegalStateException(
                    "Phiên đấu giá không nhận giá ở trạng thái " + state + ".");
        }
        if (!now.isBefore(endTime)) {
            throw new IllegalStateException("Phiên đấu giá đã hết giờ.");
        }
        if (bidderId.equals(item.getSellerId())) {
            throw new IllegalArgumentException("Người bán không được tự đặt giá sản phẩm của mình.");
        }
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Số tiền đặt giá không hợp lệ.");
        }
        boolean firstBid = bidHistory.isEmpty();
        if (firstBid && amount < currentPrice) {
            throw new IllegalArgumentException(String.format(
                    "Giá đầu tiên phải từ %.2f (giá khởi điểm) trở lên.", currentPrice));
        }
        if (!firstBid && amount <= currentPrice) {
            throw new IllegalArgumentException(String.format(
                    "Giá đặt phải lớn hơn giá hiện tại %.2f.", currentPrice));
        }

        BidTransaction bid = new BidTransaction(null, getId(), bidderId, amount, now, autoBid);
        bidHistory.add(bid);
        currentPrice = amount;
        leadingBidderId = bidderId;
        return bid;
    }

    public BidTransaction placeBid(String bidderId, double amount, boolean autoBid) {
        return placeBid(bidderId, amount, autoBid, LocalDateTime.now());
    }

    public synchronized void transitionTo(AuctionState next) {
        if (!state.canTransitionTo(next)) {
            throw new IllegalStateException(
                    String.format("Không thể chuyển trạng thái từ %s sang %s.", state, next));
        }
        state = next;
    }

    public synchronized void extendEndTime(LocalDateTime newEndTime) {
        if (newEndTime == null || !newEndTime.isAfter(endTime)) {
            throw new IllegalArgumentException(
                    "Thời gian kết thúc mới phải sau thời gian kết thúc hiện tại.");
        }
        endTime = newEndTime;
    }

    public synchronized boolean isExpired(LocalDateTime now) {
        return !now.isBefore(endTime);
    }

    public synchronized AuctionStatusDTO toStatusDTO(String leadingBidderName) {
        return new AuctionStatusDTO(getId(), item.getName(), currentPrice,
                leadingBidderName, state, endTime, bidHistory.size());
    }

    public Item getItem() { return item; }

    public String getItemId() { return item.getId(); }

    public String getSellerId() { return item.getSellerId(); }

    public LocalDateTime getStartTime() { return startTime; }

    public synchronized LocalDateTime getEndTime() { return endTime; }

    public synchronized AuctionState getState() { return state; }

    public synchronized double getCurrentPrice() { return currentPrice; }

    public synchronized String getLeadingBidderId() { return leadingBidderId; }

    public synchronized int getBidCount() { return bidHistory.size(); }

    public synchronized List<BidTransaction> getBidHistory() { return List.copyOf(bidHistory); }

    @Override
    public synchronized String printInfo() {
        return String.format("[AUCTION] ID: %s | %s | Giá hiện tại: %.2f | Trạng thái: %s | Kết thúc: %s",
                getId(), item.getName(), currentPrice, state, endTime);
    }
}