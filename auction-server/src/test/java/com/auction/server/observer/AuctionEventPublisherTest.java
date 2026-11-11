package com.auction.server.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auction.common.dto.AuctionStatusDTO;
import com.auction.common.enums.AuctionState;

class AuctionEventPublisherTest {
    private static final String AUCTION = "auction-1";

    private AuctionEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new AuctionEventPublisher();
    }

    private AuctionStatusDTO status(String auctionId, double price) {
        return new AuctionStatusDTO(auctionId, "Laptop", price, "An",
                AuctionState.RUNNING, LocalDateTime.now().plusMinutes(10), 1);
    }

    private ClientObserver client(String id, List<AuctionStatusDTO> inbox) {
        return new ClientObserver(id, inbox::add);
    }

    @Test
    void publishWithNoSubscribersDeliversToNobody() {
        assertEquals(0, publisher.publish(status(AUCTION, 100)));
    }

    @Test
    void everySubscriberReceivesTheUpdate() {
        List<AuctionStatusDTO> inboxA = new CopyOnWriteArrayList<>();
        List<AuctionStatusDTO> inboxB = new CopyOnWriteArrayList<>();
        List<AuctionStatusDTO> inboxC = new CopyOnWriteArrayList<>();
        publisher.subscribe(AUCTION, client("A", inboxA));
        publisher.subscribe(AUCTION, client("B", inboxB));
        publisher.subscribe(AUCTION, client("C", inboxC));

        int delivered = publisher.publish(status(AUCTION, 1_250_000));

        assertEquals(3, delivered);
        assertEquals(1, inboxA.size());
        assertEquals(1, inboxB.size());
        assertEquals(1, inboxC.size());
        assertEquals(1_250_000.0, inboxA.get(0).getCurrentPrice(), 0.0001);
    }

    @Test
    void onlySubscribersOfThatAuctionReceiveTheUpdate() {
        List<AuctionStatusDTO> inboxA = new CopyOnWriteArrayList<>();
        List<AuctionStatusDTO> inboxB = new CopyOnWriteArrayList<>();
        publisher.subscribe("auction-1", client("A", inboxA));
        publisher.subscribe("auction-2", client("B", inboxB));

        publisher.publish(status("auction-1", 100));

        assertEquals(1, inboxA.size());
        assertTrue(inboxB.isEmpty());
    }

    @Test
    void unsubscribedClientStopsReceiving() {
        List<AuctionStatusDTO> inboxA = new CopyOnWriteArrayList<>();
        List<AuctionStatusDTO> inboxB = new CopyOnWriteArrayList<>();
        ClientObserver a = client("A", inboxA);
        publisher.subscribe(AUCTION, a);
        publisher.subscribe(AUCTION, client("B", inboxB));

        assertTrue(publisher.unsubscribe(AUCTION, a));
        publisher.publish(status(AUCTION, 100));

        assertTrue(inboxA.isEmpty());
        assertEquals(1, inboxB.size());
        assertFalse(publisher.unsubscribe(AUCTION, a));
    }

    @Test
    void subscribingTheSameClientTwiceCountsOnce() {
        List<AuctionStatusDTO> inbox = new CopyOnWriteArrayList<>();
        publisher.subscribe(AUCTION, client("A", inbox));
        publisher.subscribe(AUCTION, client("A", inbox));

        assertEquals(1, publisher.subscriberCount(AUCTION));
        publisher.publish(status(AUCTION, 100));
        assertEquals(1, inbox.size());
    }

    @Test
    void brokenClientIsRemovedAndOthersStillReceive() {
        List<AuctionStatusDTO> inboxB = new CopyOnWriteArrayList<>();
        publisher.subscribe(AUCTION, new ClientObserver("broken", s -> {
            throw new IllegalStateException("connection lost");
        }));
        publisher.subscribe(AUCTION, client("B", inboxB));

        int delivered = publisher.publish(status(AUCTION, 100));

        assertEquals(1, delivered);
        assertEquals(1, inboxB.size());
        assertEquals(1, publisher.subscriberCount(AUCTION));
    }

    @Test
    void unsubscribeAllRemovesClientFromEveryAuction() {
        List<AuctionStatusDTO> inbox = new CopyOnWriteArrayList<>();
        ClientObserver a = client("A", inbox);
        publisher.subscribe("auction-1", a);
        publisher.subscribe("auction-2", a);

        publisher.unsubscribeAll(a);

        assertEquals(0, publisher.subscriberCount("auction-1"));
        assertEquals(0, publisher.subscriberCount("auction-2"));
    }

    @Test
    void clearAuctionRemovesEverySubscriber() {
        publisher.subscribe(AUCTION, client("A", new CopyOnWriteArrayList<>()));
        publisher.subscribe(AUCTION, client("B", new CopyOnWriteArrayList<>()));

        publisher.clearAuction(AUCTION);

        assertEquals(0, publisher.subscriberCount(AUCTION));
        assertEquals(0, publisher.publish(status(AUCTION, 100)));
    }

    @Test
    void invalidArgumentsAreRejected() {
        ClientObserver observer = client("A", new CopyOnWriteArrayList<>());
        assertThrows(IllegalArgumentException.class, () -> publisher.subscribe(" ", observer));
        assertThrows(NullPointerException.class, () -> publisher.subscribe(AUCTION, null));
        assertThrows(NullPointerException.class, () -> publisher.publish(null));
        assertThrows(IllegalArgumentException.class, () -> new ClientObserver("", s -> { }));
        assertThrows(NullPointerException.class, () -> new ClientObserver("A", null));
    }

    @Test
    void concurrentSubscribeAndPublishKeepEverySubscriber() throws InterruptedException {
        int clients = 50;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch done = new CountDownLatch(clients);

        for (int i = 0; i < clients; i++) {
            final int index = i;
            pool.submit(() -> {
                publisher.subscribe(AUCTION, new ClientObserver("C" + index, s -> { }));
                publisher.publish(status(AUCTION, 100 + index));
                done.countDown();
            });
        }

        assertTrue(done.await(5, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals(clients, publisher.subscriberCount(AUCTION));
    }
}
