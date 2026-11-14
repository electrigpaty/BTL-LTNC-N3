package com.auction.server.service;

import com.auction.common.enums.AuctionState;
import com.auction.common.model.user.Bidder;

import java.util.Objects;

public class PaymentService {

    public synchronized AuctionState processWinnerPayment(AuctionState currentState, Bidder winner, double winningAmount) {
        Objects.requireNonNull(currentState, "Current auction state must not be null.");
        if (!currentState.canTransitionTo(AuctionState.PAID)) {
            throw new IllegalStateException("Chỉ có thể thanh toán khi phiên đấu giá ở trạng thái FINISHED.");
        }
        Objects.requireNonNull(winner, "Người thắng phiên đấu giá không được null.");
        if (!Double.isFinite(winningAmount) || winningAmount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }
        if (winner.getWalletBalance() < winningAmount) {
            throw new IllegalStateException("Số dư ví không đủ để thanh toán.");
        }

        winner.setWalletBalance(winner.getWalletBalance() - winningAmount);
        return AuctionState.PAID;
    }
}