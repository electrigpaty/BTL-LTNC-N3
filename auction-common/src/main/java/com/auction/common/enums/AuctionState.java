package com.auction.common.enums;

import java.util.EnumSet;
import java.util.Set;


public enum AuctionState {
    OPEN,
    RUNNING,
    FINISHED,
    PAID,
    CANCELED;


    public Set<AuctionState> nextStates() {
        return switch (this) {
            case OPEN -> EnumSet.of(RUNNING, CANCELED);
            case RUNNING -> EnumSet.of(FINISHED, CANCELED);
            case FINISHED -> EnumSet.of(PAID, CANCELED);
            case PAID, CANCELED -> EnumSet.noneOf(AuctionState.class);
        };
    }

    public boolean canTransitionTo(AuctionState next) {
        return next != null && nextStates().contains(next);
    }

    public boolean isTerminal() {
        return nextStates().isEmpty();
    }

    public boolean acceptsBids() {
        return this == RUNNING;
    }
}