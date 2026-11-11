package com.auction.server.service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public final class AutoBidConfig implements Comparable<AutoBidConfig> {
    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final String auctionId;
    private final String bidderId;
    private final double maxBid;
    private final double increment;
    private final Instant registeredAt;
    private final long sequence;

    public AutoBidConfig(String auctionId, String bidderId, double maxBid, double increment) {
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
        this.registeredAt = Instant.now();
        this.sequence = SEQUENCE.incrementAndGet();
    }

    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public double getMaxBid() { return maxBid; }
    public double getIncrement() { return increment; }
    public Instant getRegisteredAt() { return registeredAt; }
    public long getSequence() { return sequence; }

    @Override
    public int compareTo(AutoBidConfig other) {
        int byMax = Double.compare(other.maxBid, this.maxBid);
        if (byMax != 0) {
            return byMax;
        }
        return Long.compare(this.sequence, other.sequence);
    }

    @Override
    public String toString() {
        return "AutoBidConfig{auction=" + auctionId
                + ", bidder=" + bidderId
                + ", maxBid=" + maxBid
                + ", increment=" + increment
                + ", registeredAt=" + registeredAt + "}";
    }
}
