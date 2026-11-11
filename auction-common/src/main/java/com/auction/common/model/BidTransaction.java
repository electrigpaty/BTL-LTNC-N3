package com.auction.common.model;

import java.time.LocalDateTime;


public class BidTransaction extends Entity {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String bidderId;
    private final double amount;
    private final LocalDateTime bidTime;
    private final boolean autoBid;

    public BidTransaction(String auctionId, String bidderId, double amount, boolean autoBid) {
        this(null, auctionId, bidderId, amount, LocalDateTime.now(), autoBid);
    }

    public BidTransaction(String id, String auctionId, String bidderId, double amount,
                          LocalDateTime bidTime, boolean autoBid) {
        super(id);
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("Mã phiên đấu giá không được để trống.");
        }
        if (bidderId == null || bidderId.isBlank()) {
            throw new IllegalArgumentException("Mã người đặt giá không được để trống.");
        }
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Số tiền đặt giá phải lớn hơn 0.");
        }
        if (bidTime == null) {
            throw new IllegalArgumentException("Thời điểm đặt giá không được null.");
        }
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidTime = bidTime;
        this.autoBid = autoBid;
    }

    public String getAuctionId() { return auctionId; }

    public String getBidderId() { return bidderId; }

    public double getAmount() { return amount; }

    public LocalDateTime getBidTime() { return bidTime; }

    public boolean isAutoBid() { return autoBid; }

    @Override
    public String printInfo() {
        return String.format("[BID] Phiên: %s | Người đặt: %s | Giá: %.2f | Lúc: %s%s",
                auctionId, bidderId, amount, bidTime, autoBid ? " | Auto-bid" : "");
    }
}