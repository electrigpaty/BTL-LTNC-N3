package com.auction.common.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class AuctionStateTest {

    @Test
    void openCanOnlyGoToRunningOrCanceled() {
        assertEquals(EnumSet.of(AuctionState.RUNNING, AuctionState.CANCELED), AuctionState.OPEN.nextStates());
    }

    @Test
    void runningCanOnlyGoToFinishedOrCanceled() {
        assertEquals(EnumSet.of(AuctionState.FINISHED, AuctionState.CANCELED), AuctionState.RUNNING.nextStates());
    }

    @Test
    void finishedCanOnlyGoToPaidOrCanceled() {
        assertEquals(EnumSet.of(AuctionState.PAID, AuctionState.CANCELED), AuctionState.FINISHED.nextStates());
    }

    @Test
    void paidAndCanceledHaveNoNextState() {
        assertTrue(AuctionState.PAID.nextStates().isEmpty());
        assertTrue(AuctionState.CANCELED.nextStates().isEmpty());
    }

    @Test
    void normalLifecycleIsAllowed() {
        assertTrue(AuctionState.OPEN.canTransitionTo(AuctionState.RUNNING));
        assertTrue(AuctionState.RUNNING.canTransitionTo(AuctionState.FINISHED));
        assertTrue(AuctionState.FINISHED.canTransitionTo(AuctionState.PAID));
    }

    @Test
    void cancelIsAllowedUntilPaid() {
        assertTrue(AuctionState.OPEN.canTransitionTo(AuctionState.CANCELED));
        assertTrue(AuctionState.RUNNING.canTransitionTo(AuctionState.CANCELED));
        assertTrue(AuctionState.FINISHED.canTransitionTo(AuctionState.CANCELED));
        assertFalse(AuctionState.PAID.canTransitionTo(AuctionState.CANCELED));
    }

    @Test
    void goingBackwardIsForbidden() {
        assertFalse(AuctionState.PAID.canTransitionTo(AuctionState.RUNNING));
        assertFalse(AuctionState.PAID.canTransitionTo(AuctionState.FINISHED));
        assertFalse(AuctionState.FINISHED.canTransitionTo(AuctionState.RUNNING));
        assertFalse(AuctionState.RUNNING.canTransitionTo(AuctionState.OPEN));
        assertFalse(AuctionState.CANCELED.canTransitionTo(AuctionState.OPEN));
    }

    @Test
    void skippingAStepIsForbidden() {
        assertFalse(AuctionState.OPEN.canTransitionTo(AuctionState.FINISHED));
        assertFalse(AuctionState.OPEN.canTransitionTo(AuctionState.PAID));
        assertFalse(AuctionState.RUNNING.canTransitionTo(AuctionState.PAID));
    }

    @Test
    void stayingInTheSameStateIsForbidden() {
        for (AuctionState state : AuctionState.values()) {
            assertFalse(state.canTransitionTo(state));
        }
    }

    @Test
    void nullTargetIsForbidden() {
        for (AuctionState state : AuctionState.values()) {
            assertFalse(state.canTransitionTo(null));
        }
    }

    @Test
    void canTransitionToAgreesWithNextStatesForEveryPair() {
        for (AuctionState from : AuctionState.values()) {
            for (AuctionState to : AuctionState.values()) {
                assertEquals(from.nextStates().contains(to), from.canTransitionTo(to));
            }
        }
    }

    @Test
    void onlyPaidAndCanceledAreTerminal() {
        assertFalse(AuctionState.OPEN.isTerminal());
        assertFalse(AuctionState.RUNNING.isTerminal());
        assertFalse(AuctionState.FINISHED.isTerminal());
        assertTrue(AuctionState.PAID.isTerminal());
        assertTrue(AuctionState.CANCELED.isTerminal());
    }

    @Test
    void onlyRunningAcceptsBids() {
        for (AuctionState state : AuctionState.values()) {
            assertEquals(state == AuctionState.RUNNING, state.acceptsBids());
        }
    }

    @Test
    void changingTheReturnedSetDoesNotChangeTheRules() {
        Set<AuctionState> next = AuctionState.OPEN.nextStates();
        next.add(AuctionState.PAID);

        assertFalse(AuctionState.OPEN.canTransitionTo(AuctionState.PAID));
        assertEquals(EnumSet.of(AuctionState.RUNNING, AuctionState.CANCELED), AuctionState.OPEN.nextStates());
    }
}
