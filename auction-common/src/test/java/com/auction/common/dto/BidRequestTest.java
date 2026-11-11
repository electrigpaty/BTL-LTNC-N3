package com.auction.common.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class BidRequestTest {

    @Test
    void validRequestKeepsItsValues() {
        BidRequest request = new BidRequest("auction-1", "an", 1_200_000);

        assertEquals("auction-1", request.getAuctionId());
        assertEquals("an", request.getBidderId());
        assertEquals(1_200_000.0, request.getAmount(), 0.0001);
    }

    @Test
    void blankOrNullIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BidRequest(null, "an", 100));
        assertThrows(IllegalArgumentException.class, () -> new BidRequest(" ", "an", 100));
        assertThrows(IllegalArgumentException.class, () -> new BidRequest("auction-1", null, 100));
        assertThrows(IllegalArgumentException.class, () -> new BidRequest("auction-1", "", 100));
    }

    @Test
    void invalidAmountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BidRequest("auction-1", "an", 0));
        assertThrows(IllegalArgumentException.class, () -> new BidRequest("auction-1", "an", -10));
        assertThrows(IllegalArgumentException.class, () -> new BidRequest("auction-1", "an", Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new BidRequest("auction-1", "an", Double.POSITIVE_INFINITY));
    }

    @Test
    void survivesSerializationRoundTrip() throws IOException, ClassNotFoundException {
        BidRequest original = new BidRequest("auction-1", "an", 1_200_000);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        BidRequest copy;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            copy = (BidRequest) in.readObject();
        }

        assertEquals(original.getAuctionId(), copy.getAuctionId());
        assertEquals(original.getBidderId(), copy.getBidderId());
        assertEquals(original.getAmount(), copy.getAmount(), 0.0001);
    }
}
