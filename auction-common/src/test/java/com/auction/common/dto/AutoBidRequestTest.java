package com.auction.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class AutoBidRequestTest {

    @Test
    void validRequestKeepsItsValues() {
        AutoBidRequest request = new AutoBidRequest("auction-1", "binh", 1_500_000, 50_000);

        assertEquals("auction-1", request.getAuctionId());
        assertEquals("binh", request.getBidderId());
        assertEquals(1_500_000.0, request.getMaxBid(), 0.0001);
        assertEquals(50_000.0, request.getIncrement(), 0.0001);
    }

    @Test
    void blankOrNullIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest(null, "binh", 100, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("  ", "binh", 100, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", null, 100, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", "", 100, 10));
    }

    @Test
    void invalidMaxBidIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", "binh", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", "binh", -5, 10));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", "binh", Double.NaN, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new AutoBidRequest("auction-1", "binh", Double.POSITIVE_INFINITY, 10));
    }

    @Test
    void invalidIncrementIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", "binh", 100, 0));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", "binh", 100, -1));
        assertThrows(IllegalArgumentException.class, () -> new AutoBidRequest("auction-1", "binh", 100, Double.NaN));
    }

    @Test
    void survivesSerializationRoundTrip() throws IOException, ClassNotFoundException {
        AutoBidRequest original = new AutoBidRequest("auction-1", "binh", 1_500_000, 50_000);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        AutoBidRequest copy;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            copy = (AutoBidRequest) in.readObject();
        }

        assertEquals(original.getAuctionId(), copy.getAuctionId());
        assertEquals(original.getBidderId(), copy.getBidderId());
        assertEquals(original.getMaxBid(), copy.getMaxBid(), 0.0001);
        assertEquals(original.getIncrement(), copy.getIncrement(), 0.0001);
    }
}
