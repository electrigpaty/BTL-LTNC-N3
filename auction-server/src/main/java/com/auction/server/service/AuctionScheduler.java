package com.auction.server.service;

import com.auction.common.enums.AuctionState;
import com.auction.common.model.Auction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AuctionScheduler {
    private static final long TICK_SECONDS = 1;

    private final Supplier<Collection<Auction>> auctionSource;
    private final Consumer<Auction> onStateChanged;
    private ScheduledExecutorService executor;

    public AuctionScheduler(Supplier<Collection<Auction>> auctionSource,
                            Consumer<Auction> onStateChanged) {
        this.auctionSource = Objects.requireNonNull(auctionSource, "auctionSource không được null.");
        this.onStateChanged = Objects.requireNonNull(onStateChanged, "onStateChanged không được null.");
    }

    public synchronized void start() {
        if (executor != null) {
            return;
        }
        executor = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "auction-scheduler");
            thread.setDaemon(true); // không giữ cho server khỏi tắt
            return thread;
        });
        executor.scheduleAtFixedRate(this::safeTick, 0, TICK_SECONDS, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public int tick(LocalDateTime now) {
        Objects.requireNonNull(now, "Thời điểm không được null.");
        int changed = 0;
        for (Auction auction : new ArrayList<>(auctionSource.get())) {
            if (advance(auction, now)) {
                changed++;
                // Gọi bên ngoài khối synchronized để listener (ghi DB, ghi Socket)
                // không giữ khóa của phiên và gây nghẽn hoặc deadlock.
                try {
                    onStateChanged.accept(auction);
                } catch (RuntimeException e) {
                    System.err.println("Lỗi khi xử lý phiên " + auction.getId() + ": " + e.getMessage());
                }
            }
        }
        return changed;
    }

    private boolean advance(Auction auction, LocalDateTime now) {
        boolean changed = false;
        // Cùng khóa với placeBid và AntiSniping: một bid phút chót vừa gia hạn
        // giờ kết thúc thì lượt quét này sẽ thấy giờ mới, không đóng nhầm phiên.
        synchronized (auction) {
            if (auction.getState() == AuctionState.OPEN && !now.isBefore(auction.getStartTime())) {
                auction.transitionTo(AuctionState.RUNNING);
                changed = true;
            }
            // Nếu server tắt rồi bật lại muộn, một phiên có thể đi liền OPEN -> RUNNING -> FINISHED.
            if (auction.getState() == AuctionState.RUNNING && auction.isExpired(now)) {
                auction.transitionTo(AuctionState.FINISHED);
                changed = true;
            }
        }
        return changed;
    }

    private void safeTick() {
        // scheduleAtFixedRate sẽ ngừng chạy vĩnh viễn nếu một lượt ném exception,
        // nên phải bắt mọi lỗi ở đây.
        try {
            tick(LocalDateTime.now());
        } catch (RuntimeException e) {
            System.err.println("Scheduler gặp lỗi: " + e.getMessage());
        }
    }
}