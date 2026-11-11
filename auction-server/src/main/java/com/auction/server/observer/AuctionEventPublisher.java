package com.auction.server.observer;

import com.auction.common.dto.AuctionStatusDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionEventPublisher {
    private final Map<String, Set<AuctionObserver>> observersByAuction = new ConcurrentHashMap<>();

    public void subscribe(String auctionId, AuctionObserver observer) {
        requireAuctionId(auctionId);
        Objects.requireNonNull(observer, "Observer must not be null.");
        observersByAuction.compute(auctionId, (id, observers) -> {
            Set<AuctionObserver> target = observers == null ? new CopyOnWriteArraySet<>() : observers;
            target.add(observer);
            return target;
        });
    }

    public boolean unsubscribe(String auctionId, AuctionObserver observer) {
        if (auctionId == null || observer == null) {
            return false;
        }
        AtomicBoolean removed = new AtomicBoolean(false);
        observersByAuction.computeIfPresent(auctionId, (id, observers) -> {
            removed.set(observers.remove(observer));
            return observers.isEmpty() ? null : observers;
        });
        return removed.get();
    }

    public void unsubscribeAll(AuctionObserver observer) {
        if (observer == null) {
            return;
        }
        for (String auctionId : new ArrayList<>(observersByAuction.keySet())) {
            unsubscribe(auctionId, observer);
        }
    }

    public int publish(AuctionStatusDTO status) {
        Objects.requireNonNull(status, "Status must not be null.");
        Set<AuctionObserver> observers = observersByAuction.get(status.getAuctionId());
        if (observers == null) {
            return 0;
        }

        int delivered = 0;
        List<AuctionObserver> failed = new ArrayList<>();
        for (AuctionObserver observer : observers) {
            try {
                observer.onUpdate(status);
                delivered++;
            } catch (RuntimeException e) {
                failed.add(observer);
            }
        }

        for (AuctionObserver observer : failed) {
            unsubscribe(status.getAuctionId(), observer);
        }
        return delivered;
    }

    public void clearAuction(String auctionId) {
        if (auctionId != null) {
            observersByAuction.remove(auctionId);
        }
    }

    public int subscriberCount(String auctionId) {
        Set<AuctionObserver> observers = observersByAuction.get(auctionId);
        return observers == null ? 0 : observers.size();
    }

    private void requireAuctionId(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("Auction id must not be blank.");
        }
    }
}
