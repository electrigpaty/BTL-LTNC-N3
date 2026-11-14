package com.auction.server.service;

import com.auction.common.enums.AuctionState;
import com.auction.common.model.Auction;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;


public class AntiSniping {
    public static final Duration DEFAULT_WINDOW = Duration.ofSeconds(30);
    public static final Duration DEFAULT_EXTENSION = Duration.ofSeconds(60);

    private final Duration window;
    private final Duration extension;

    public AntiSniping() {
        this(DEFAULT_WINDOW, DEFAULT_EXTENSION);
    }

    public AntiSniping(Duration window, Duration extension) {
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Khoảng thời gian kiểm tra phải lớn hơn 0.");
        }
        if (extension == null || extension.isZero() || extension.isNegative()) {
            throw new IllegalArgumentException("Thời gian gia hạn phải lớn hơn 0.");
        }
        this.window = window;
        this.extension = extension;
    }


    public boolean extendIfNeeded(Auction auction, LocalDateTime bidTime) {
        Objects.requireNonNull(auction, "Phiên đấu giá không được null.");
        Objects.requireNonNull(bidTime, "Thời điểm bid không được null.");
        // Auction dùng synchronized trên chính nó, nên khóa cùng đối tượng
        // để việc đọc giờ kết thúc và gia hạn là một bước không bị chen ngang.
        synchronized (auction) {
            if (auction.getState() != AuctionState.RUNNING) {
                return false;
            }
            LocalDateTime endTime = auction.getEndTime();
            boolean inLastWindow = bidTime.isBefore(endTime)
                    && !bidTime.isBefore(endTime.minus(window));
            if (!inLastWindow) {
                return false;
            }
            auction.extendEndTime(endTime.plus(extension));
            return true;
        }
    }

    public Duration getWindow() { return window; }

    public Duration getExtension() { return extension; }
}