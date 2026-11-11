package com.auction.server.observer;

import com.auction.common.dto.AuctionStatusDTO;

import java.util.Objects;
import java.util.function.Consumer;

public class ClientObserver implements AuctionObserver {
    private final String clientId;
    private final Consumer<AuctionStatusDTO> sender;

    public ClientObserver(String clientId, Consumer<AuctionStatusDTO> sender) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client id must not be blank.");
        }
        this.clientId = clientId;
        this.sender = Objects.requireNonNull(sender, "Sender must not be null.");
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public void onUpdate(AuctionStatusDTO status) {
        sender.accept(status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ClientObserver)) return false;
        return clientId.equals(((ClientObserver) obj).clientId);
    }

    @Override
    public int hashCode() {
        return clientId.hashCode();
    }

    @Override
    public String toString() {
        return "ClientObserver{clientId=" + clientId + "}";
    }
}
