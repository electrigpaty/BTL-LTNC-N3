package com.auction.common.dto;

import com.auction.common.enums.Command;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String requestId;
    private final Command command;
    private final Object payload;

    public Request(Command command, Object payload) {
        this(UUID.randomUUID().toString(), command, payload);
    }

    public Request(String requestId, Command command, Object payload) {
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("Request id must not be blank.");
        }
        this.requestId = requestId;
        this.command = Objects.requireNonNull(command, "Command must not be null.");
        this.payload = payload;
    }

    public String getRequestId() { return requestId; }

    public Command getCommand() { return command; }

    public Object getPayload() { return payload; }

    @Override
    public String toString() {
        return String.format("Request{id=%s, command=%s, payload=%s}", requestId, command, payload);
    }
}