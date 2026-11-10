package com.auction.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class AutoBidEngine {
    private static final int MAX_ROUNDS = 10_000;

    public record AutoBid(String bidderId, double amount) {}

    private final Map<String, PriorityQueue<AutoBidConfig>> queuesByAuction = new HashMap<>();

    public synchronized void register(AutoBidConfig config) {
        Objects.requireNonNull(config, "Config must not be null.");
        PriorityQueue<AutoBidConfig> queue =
                queuesByAuction.computeIfAbsent(config.getAuctionId(), id -> new PriorityQueue<>());
        queue.removeIf(existing -> existing.getBidderId().equals(config.getBidderId()));
        queue.add(config);
    }

    public synchronized boolean unregister(String auctionId, String bidderId) {
        PriorityQueue<AutoBidConfig> queue = queuesByAuction.get(auctionId);
        if (queue == null) {
            return false;
        }
        boolean removed = queue.removeIf(existing -> existing.getBidderId().equals(bidderId));
        if (queue.isEmpty()) {
            queuesByAuction.remove(auctionId);
        }
        return removed;
    }

    public synchronized void clearAuction(String auctionId) {
        queuesByAuction.remove(auctionId);
    }

    public synchronized boolean isRegistered(String auctionId, String bidderId) {
        PriorityQueue<AutoBidConfig> queue = queuesByAuction.get(auctionId);
        if (queue == null) {
            return false;
        }
        return queue.stream().anyMatch(config -> config.getBidderId().equals(bidderId));
    }

    public synchronized int registeredCount(String auctionId) {
        PriorityQueue<AutoBidConfig> queue = queuesByAuction.get(auctionId);
        return queue == null ? 0 : queue.size();
    }

    public synchronized List<AutoBid> resolve(String auctionId, double currentPrice, String leaderId) {
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("Auction id must not be blank.");
        }
        if (!Double.isFinite(currentPrice) || currentPrice < 0) {
            throw new IllegalArgumentException("Current price must not be negative.");
        }

        List<AutoBid> placed = new ArrayList<>();
        PriorityQueue<AutoBidConfig> queue = queuesByAuction.get(auctionId);
        if (queue == null || queue.isEmpty()) {
            return placed;
        }

        List<AutoBidConfig> ranked = rankByPriority(queue);
        double price = currentPrice;
        String leader = leaderId;

        for (int round = 0; round < MAX_ROUNDS; round++) {
            AutoBidConfig leaderConfig = findConfig(ranked, leader);
            AutoBidConfig chosen = null;
            double chosenAmount = 0;

            for (AutoBidConfig candidate : ranked) {
                if (candidate.getBidderId().equals(leader)) {
                    continue;
                }
                double amount = Math.min(price + candidate.getIncrement(), candidate.getMaxBid());
                boolean beatsPrice = amount > price;
                boolean winsTie = amount == price
                        && leaderConfig != null
                        && leaderConfig.getMaxBid() == price
                        && candidate.compareTo(leaderConfig) < 0;
                if (beatsPrice || winsTie) {
                    chosen = candidate;
                    chosenAmount = amount;
                    break;
                }
            }

            if (chosen == null) {
                break;
            }

            placed.add(new AutoBid(chosen.getBidderId(), chosenAmount));
            price = chosenAmount;
            leader = chosen.getBidderId();
        }

        return placed;
    }

    private List<AutoBidConfig> rankByPriority(PriorityQueue<AutoBidConfig> queue) {
        PriorityQueue<AutoBidConfig> copy = new PriorityQueue<>(queue);
        List<AutoBidConfig> ranked = new ArrayList<>(copy.size());
        while (!copy.isEmpty()) {
            ranked.add(copy.poll());
        }
        return ranked;
    }

    private AutoBidConfig findConfig(List<AutoBidConfig> ranked, String bidderId) {
        if (bidderId == null) {
            return null;
        }
        for (AutoBidConfig config : ranked) {
            if (config.getBidderId().equals(bidderId)) {
                return config;
            }
        }
        return null;
    }
}
