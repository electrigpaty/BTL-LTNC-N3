package com.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auction.server.service.AutoBidEngine.AutoBid;

class AutoBidEngineTest {
    private static final String AUCTION = "auction-1";

    private AutoBidEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AutoBidEngine();
    }

    private AutoBidConfig config(String bidderId, double maxBid, double increment) {
        return new AutoBidConfig(AUCTION, bidderId, maxBid, increment);
    }

    private AutoBid last(List<AutoBid> bids) {
        return bids.get(bids.size() - 1);
    }

    @Test
    void noRegisteredConfigsProducesNoBids() {
        assertTrue(engine.resolve(AUCTION, 100, "manual").isEmpty());
    }

    @Test
    void singleAutoBidderOutbidsManualLeaderByOneIncrement() {
        engine.register(config("A", 500, 10));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        assertEquals(1, bids.size());
        assertEquals("A", bids.get(0).bidderId());
        assertEquals(110.0, bids.get(0).amount(), 0.0001);
    }

    @Test
    void autoBidderDoesNotBidAgainstItself() {
        engine.register(config("A", 500, 10));

        assertTrue(engine.resolve(AUCTION, 100, "A").isEmpty());
    }

    @Test
    void autoBidIsCappedAtMaxBid() {
        engine.register(config("A", 105, 10));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        assertEquals(1, bids.size());
        assertEquals(105.0, bids.get(0).amount(), 0.0001);
    }

    @Test
    void autoBidderStopsWhenPriceReachesMaxBid() {
        engine.register(config("A", 200, 10));

        assertTrue(engine.resolve(AUCTION, 200, "manual").isEmpty());
        assertTrue(engine.resolve(AUCTION, 250, "manual").isEmpty());
    }

    @Test
    void higherMaxBidWinsAndPaysJustAboveTheRunnerUp() {
        engine.register(config("A", 500, 10));
        engine.register(config("B", 300, 20));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        assertEquals("A", last(bids).bidderId());
        assertEquals(310.0, last(bids).amount(), 0.0001);
    }

    @Test
    void noAutoBidEverExceedsItsOwnMaxBid() {
        engine.register(config("A", 500, 10));
        engine.register(config("B", 300, 20));
        engine.register(config("C", 420, 15));
        Map<String, Double> maxByBidder = new HashMap<>();
        maxByBidder.put("A", 500.0);
        maxByBidder.put("B", 300.0);
        maxByBidder.put("C", 420.0);

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        assertFalse(bids.isEmpty());
        for (AutoBid bid : bids) {
            assertTrue(bid.amount() <= maxByBidder.get(bid.bidderId()));
        }
    }

    @Test
    void bidAmountsNeverGoDown() {
        engine.register(config("A", 500, 10));
        engine.register(config("B", 300, 20));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        for (int i = 1; i < bids.size(); i++) {
            assertTrue(bids.get(i).amount() >= bids.get(i - 1).amount());
        }
    }

    @Test
    void sameBidderNeverBidsTwiceInARow() {
        engine.register(config("A", 500, 10));
        engine.register(config("B", 300, 20));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        for (int i = 1; i < bids.size(); i++) {
            assertFalse(bids.get(i).bidderId().equals(bids.get(i - 1).bidderId()));
        }
    }

    @Test
    void equalMaxBidEarlierRegistrationWins() {
        engine.register(config("A", 300, 10));
        engine.register(config("B", 300, 10));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        assertEquals("A", last(bids).bidderId());
        assertEquals(300.0, last(bids).amount(), 0.0001);
    }

    @Test
    void equalMaxBidEarlierRegistrationWinsEvenWhenItIsTheSecondName() {
        engine.register(config("B", 300, 10));
        engine.register(config("A", 300, 10));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        assertEquals("B", last(bids).bidderId());
        assertEquals(300.0, last(bids).amount(), 0.0001);
    }

    @Test
    void registeringAgainReplacesThePreviousConfig() {
        engine.register(config("A", 150, 10));
        engine.register(config("A", 400, 50));

        List<AutoBid> bids = engine.resolve(AUCTION, 100, "manual");

        assertEquals(1, engine.registeredCount(AUCTION));
        assertEquals(1, bids.size());
        assertEquals(150.0, bids.get(0).amount(), 0.0001);
    }

    @Test
    void unregisterStopsFurtherBidding() {
        engine.register(config("A", 500, 10));

        assertTrue(engine.unregister(AUCTION, "A"));

        assertFalse(engine.isRegistered(AUCTION, "A"));
        assertTrue(engine.resolve(AUCTION, 100, "manual").isEmpty());
        assertFalse(engine.unregister(AUCTION, "A"));
    }

    @Test
    void clearAuctionRemovesEveryConfig() {
        engine.register(config("A", 500, 10));
        engine.register(config("B", 300, 20));

        engine.clearAuction(AUCTION);

        assertEquals(0, engine.registeredCount(AUCTION));
        assertTrue(engine.resolve(AUCTION, 100, "manual").isEmpty());
    }

    @Test
    void auctionsDoNotAffectEachOther() {
        engine.register(new AutoBidConfig("auction-1", "A", 500, 10));
        engine.register(new AutoBidConfig("auction-2", "B", 500, 10));

        List<AutoBid> bids = engine.resolve("auction-1", 100, "manual");

        assertEquals(1, bids.size());
        assertEquals("A", bids.get(0).bidderId());
        assertEquals(1, engine.registeredCount("auction-2"));
    }

    @Test
    void resolveDoesNotConsumeRegistrations() {
        engine.register(config("A", 500, 10));

        engine.resolve(AUCTION, 100, "manual");
        List<AutoBid> again = engine.resolve(AUCTION, 200, "manual");

        assertEquals(1, engine.registeredCount(AUCTION));
        assertEquals(1, again.size());
        assertEquals(210.0, again.get(0).amount(), 0.0001);
    }

    @Test
    void invalidConfigIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AutoBidConfig("", "A", 100, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidConfig(AUCTION, " ", 100, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidConfig(AUCTION, "A", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidConfig(AUCTION, "A", 100, -5));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidConfig(AUCTION, "A", Double.NaN, 10));
    }

    @Test
    void invalidResolveArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> engine.resolve("", 100, "manual"));
        assertThrows(IllegalArgumentException.class, () -> engine.resolve(AUCTION, -1, "manual"));
    }

    @Test
    void concurrentRegistrationsKeepEveryBidder() throws InterruptedException {
        int bidders = 20;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch done = new CountDownLatch(bidders);

        for (int i = 0; i < bidders; i++) {
            final int index = i;
            pool.submit(() -> {
                engine.register(config("B" + index, 100 + index * 10, 5));
                done.countDown();
            });
        }

        assertTrue(done.await(5, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals(bidders, engine.registeredCount(AUCTION));

        List<AutoBid> bids = engine.resolve(AUCTION, 50, "manual");
        assertEquals("B19", last(bids).bidderId());
        assertTrue(last(bids).amount() > 280 && last(bids).amount() <= 290);
    }
}
