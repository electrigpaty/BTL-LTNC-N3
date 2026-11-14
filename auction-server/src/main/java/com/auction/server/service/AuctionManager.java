package com.auction.server.service;

import com.auction.common.dto.AuctionStatusDTO;
import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.BidRequest;
import com.auction.common.enums.AuctionState;
import com.auction.server.config.ServerConfig;
import com.auction.server.observer.AuctionEventPublisher;
import com.auction.server.service.AutoBidEngine.AutoBid;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionManager {

    public static class ManagedAuction {
        private final String auctionId;
        private final String itemName;
        private final double startingPrice;
        private final double minIncrement;
        private double currentPrice;
        private String leadingBidderId;
        private AuctionState state;
        private LocalDateTime endTime;
        private int bidCount;

        public ManagedAuction(String auctionId, String itemName, double startingPrice,
                              double minIncrement, AuctionState state, LocalDateTime endTime) {
            if (auctionId == null || auctionId.isBlank()) {
                throw new IllegalArgumentException("Auction id must not be blank.");
            }
            if (!Double.isFinite(startingPrice) || startingPrice <= 0) {
                throw new IllegalArgumentException("Starting price must be positive.");
            }
            if (!Double.isFinite(minIncrement) || minIncrement <= 0) {
                throw new IllegalArgumentException("Min increment must be positive.");
            }
            this.auctionId = auctionId;
            this.itemName = itemName == null ? "" : itemName;
            this.startingPrice = startingPrice;
            this.minIncrement = minIncrement;
            this.currentPrice = startingPrice;
            this.leadingBidderId = null;
            this.state = Objects.requireNonNull(state, "State must not be null.");
            this.endTime = Objects.requireNonNull(endTime, "End time must not be null.");
            this.bidCount = 0;
        }

        public AuctionStatusDTO toStatusDTO() {
            return new AuctionStatusDTO(auctionId, itemName, currentPrice,
                    leadingBidderId, state, endTime, bidCount);
        }

        public String getAuctionId() { return auctionId; }
        public String getItemName() { return itemName; }
        public double getStartingPrice() { return startingPrice; }
        public double getMinIncrement() { return minIncrement; }
        public double getCurrentPrice() { return currentPrice; }
        public String getLeadingBidderId() { return leadingBidderId; }
        public AuctionState getState() { return state; }
        public LocalDateTime getEndTime() { return endTime; }
        public int getBidCount() { return bidCount; }
    }

    private final Map<String, ManagedAuction> auctions = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locksByAuction = new ConcurrentHashMap<>();
    private final AutoBidEngine autoBidEngine;
    private final AuctionEventPublisher eventPublisher;

    public AuctionManager() {
        this(new AutoBidEngine(), new AuctionEventPublisher());
    }

    public AuctionManager(AutoBidEngine autoBidEngine, AuctionEventPublisher eventPublisher) {
        this.autoBidEngine = Objects.requireNonNull(autoBidEngine, "AutoBidEngine must not be null.");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "AuctionEventPublisher must not be null.");
    }

    private ReentrantLock lockFor(String auctionId) {
        return locksByAuction.computeIfAbsent(auctionId, id -> new ReentrantLock(true));
    }

    public void registerAuction(ManagedAuction auction) {
        Objects.requireNonNull(auction, "Auction must not be null.");
        ReentrantLock lock = lockFor(auction.getAuctionId());
        lock.lock();
        try {
            auctions.put(auction.getAuctionId(), auction);
        } finally {
            lock.unlock();
        }
    }

    public AuctionStatusDTO placeBid(BidRequest bidRequest) {
        return placeBid(bidRequest, LocalDateTime.now());
    }

    public AuctionStatusDTO placeBid(BidRequest bidRequest, LocalDateTime bidTime) {
        Objects.requireNonNull(bidRequest, "BidRequest must not be null.");
        Objects.requireNonNull(bidTime, "Bid time must not be null.");

        String auctionId = bidRequest.getAuctionId();
        ReentrantLock lock = lockFor(auctionId);
        lock.lock();
        try {
            ManagedAuction auction = getRequiredAuction(auctionId);

            if (!auction.state.acceptsBids()) {
                throw new IllegalStateException("Phiên đấu giá không ở trạng thái RUNNING.");
            }
            if (!bidTime.isBefore(auction.endTime)) {
                throw new IllegalStateException("Phiên đấu giá đã hết thời gian.");
            }

            double minRequired = (auction.bidCount == 0)
                    ? auction.startingPrice
                    : auction.currentPrice + auction.minIncrement;

            if (bidRequest.getAmount() < minRequired) {
                throw new IllegalArgumentException(
                        String.format("Giá đặt (%.2f) phải từ %.2f trở lên.", bidRequest.getAmount(), minRequired));
            }

            auction.currentPrice = bidRequest.getAmount();
            auction.leadingBidderId = bidRequest.getBidderId();
            auction.bidCount++;

            long remainingSeconds = Duration.between(bidTime, auction.endTime).getSeconds();
            if (remainingSeconds >= 0 && remainingSeconds <= ServerConfig.ANTI_SNIPING_WINDOW_SECONDS) {
                auction.endTime = auction.endTime.plusSeconds(ServerConfig.ANTI_SNIPING_EXTENSION_SECONDS);
            }

            List<AutoBid> autoBids = autoBidEngine.resolve(
                    auctionId, auction.currentPrice, auction.leadingBidderId);
            for (AutoBid auto : autoBids) {
                auction.currentPrice = auto.amount();
                auction.leadingBidderId = auto.bidderId();
                auction.bidCount++;
            }

            AuctionStatusDTO snapshot = auction.toStatusDTO();
            eventPublisher.publish(snapshot);
            return snapshot;
        } finally {
            lock.unlock();
        }
    }

    public AuctionStatusDTO registerAutoBid(AutoBidRequest request) {
        Objects.requireNonNull(request, "AutoBidRequest must not be null.");
        String auctionId = request.getAuctionId();
        ReentrantLock lock = lockFor(auctionId);
        lock.lock();
        try {
            ManagedAuction auction = getRequiredAuction(auctionId);
            if (!auction.state.acceptsBids()) {
                throw new IllegalStateException("Chỉ có thể đặt Auto-Bid khi phiên đang RUNNING.");
            }

            AutoBidConfig config = new AutoBidConfig(
                    auctionId, request.getBidderId(), request.getMaxBid(), request.getIncrement());
            autoBidEngine.register(config);

            List<AutoBid> autoBids = autoBidEngine.resolve(
                    auctionId, auction.currentPrice, auction.leadingBidderId);
            for (AutoBid auto : autoBids) {
                auction.currentPrice = auto.amount();
                auction.leadingBidderId = auto.bidderId();
                auction.bidCount++;
            }

            AuctionStatusDTO snapshot = auction.toStatusDTO();
            if (!autoBids.isEmpty()) {
                eventPublisher.publish(snapshot);
            }
            return snapshot;
        } finally {
            lock.unlock();
        }
    }

    public AuctionStatusDTO transitionState(String auctionId, AuctionState nextState) {
        ReentrantLock lock = lockFor(auctionId);
        lock.lock();
        try {
            ManagedAuction auction = getRequiredAuction(auctionId);
            if (!auction.state.canTransitionTo(nextState)) {
                throw new IllegalStateException(
                        "Không thể chuyển trạng thái từ " + auction.state + " sang " + nextState);
            }
            auction.state = nextState;
            AuctionStatusDTO snapshot = auction.toStatusDTO();
            eventPublisher.publish(snapshot);
            return snapshot;
        } finally {
            lock.unlock();
        }
    }

    public AuctionStatusDTO getAuctionStatus(String auctionId) {
        ReentrantLock lock = lockFor(auctionId);
        lock.lock();
        try {
            return getRequiredAuction(auctionId).toStatusDTO();
        } finally {
            lock.unlock();
        }
    }

    public List<AuctionStatusDTO> listAuctionStatuses() {
        List<AuctionStatusDTO> result = new ArrayList<>();
        for (String id : auctions.keySet()) {
            result.add(getAuctionStatus(id));
        }
        return result;
    }

    public AutoBidEngine getAutoBidEngine() { return autoBidEngine; }
    public AuctionEventPublisher getEventPublisher() { return eventPublisher; }

    private ManagedAuction getRequiredAuction(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("Mã phiên đấu giá không được để trống.");
        }
        ManagedAuction auction = auctions.get(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Không tìm thấy phiên đấu giá: " + auctionId);
        }
        return auction;
    }
}