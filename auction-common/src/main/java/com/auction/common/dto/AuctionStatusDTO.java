package com.auction.common.dto;

import com.auction.common.enums.AuctionState;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


public class AuctionStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String itemName;
    private final double currentPrice;
    private final String leadingBidderName; // null nếu chưa ai đặt giá
    private final AuctionState state;
    private final LocalDateTime endTime;
    private final int bidCount;

    public AuctionStatusDTO(String auctionId, String itemName, double currentPrice,
                            String leadingBidderName, AuctionState state,
                            LocalDateTime endTime, int bidCount) {
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("Mã phiên đấu giá không được để trống.");
        }
        if (!Double.isFinite(currentPrice) || currentPrice < 0) {
            throw new IllegalArgumentException("Giá hiện tại không được âm.");
        }
        if (bidCount < 0) {
            throw new IllegalArgumentException("Số lượt đặt giá không được âm.");
        }
        this.auctionId = auctionId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.leadingBidderName = leadingBidderName;
        this.state = Objects.requireNonNull(state, "Trạng thái không được null.");
        this.endTime = Objects.requireNonNull(endTime, "Thời gian kết thúc không được null.");
        this.bidCount = bidCount;
    }

    public String getAuctionId() { return auctionId; }

    public String getItemName() { return itemName; }

    public double getCurrentPrice() { return currentPrice; }

    public String getLeadingBidderName() { return leadingBidderName; }

    public AuctionState getState() { return state; }

    public LocalDateTime getEndTime() { return endTime; }

    public int getBidCount() { return bidCount; }

    @Override
    public String toString() {
        return String.format("AuctionStatusDTO{auction=%s, item=%s, price=%.2f, leader=%s, state=%s, bids=%d}",
                auctionId, itemName, currentPrice, leadingBidderName, state, bidCount);
    }
}