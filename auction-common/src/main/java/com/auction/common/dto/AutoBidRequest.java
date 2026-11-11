package com.auction.common.dto;

import java.io.Serializable;

public class AutoBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;
    private final double maxBid;
    private final double increment;

    public AutoBidRequest(String auctionId, String bidderId, double maxBid, double increment) {
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("Auction id must not be blank.");
        }
        if (bidderId == null || bidderId.isBlank()) {
            throw new IllegalArgumentException("Bidder id must not be blank.");
        }
        if (!Double.isFinite(maxBid) || maxBid <= 0) {
            throw new IllegalArgumentException("Max bid must be a positive number.");
        }
        if (!Double.isFinite(increment) || increment <= 0) {
            throw new IllegalArgumentException("Increment must be a positive number.");
        }
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
    }

    public String getAuctionId() { return auctionId; }

    public String getBidderId() { return bidderId; }

    public double getMaxBid() { return maxBid; }

    public double getIncrement() { return increment; }

    @Override
    public String toString() {
        return String.format("AutoBidRequest{auction=%s, bidder=%s, maxBid=%.2f, increment=%.2f}",
                auctionId, bidderId, maxBid, increment);
    }
}
