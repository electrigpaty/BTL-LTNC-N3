package com.auction.common.dto;

import java.io.Serializable;


public class BidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;
    private final double amount;

    public BidRequest(String auctionId, String bidderId, double amount) {
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("Mã phiên đấu giá không được để trống.");
        }
        if (bidderId == null || bidderId.isBlank()) {
            throw new IllegalArgumentException("Mã người đặt giá không được để trống.");
        }
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Số tiền đặt giá phải lớn hơn 0.");
        }
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
    }

    public String getAuctionId() { return auctionId; }

    public String getBidderId() { return bidderId; }

    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("BidRequest{auction=%s, bidder=%s, amount=%.2f}",
                auctionId, bidderId, amount);
    }
}