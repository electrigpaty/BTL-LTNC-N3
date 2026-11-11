package com.auction.common.dto;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Object data;
    private final boolean push;

    public Response(boolean success, String message, Object data, boolean push) {
        this.success = success;
        this.message = message == null ? "" : message;
        this.data = data;
        this.push = push;
    }

    public static Response ok(String message, Object data) {
        return new Response(true, message, data, false);
    }

    public static Response ok(Object data) {
        return new Response(true, "OK", data, false);
    }

    public static Response error(String message) {
        return new Response(false, message, null, false);
    }

    public static Response push(AuctionStatusDTO status) {
        return new Response(true, "AUCTION_UPDATE", status, true);
    }

    public boolean isSuccess() { return success; }

    public String getMessage() { return message; }

    public Object getData() { return data; }

    public boolean isPush() { return push; }

    @Override
    public String toString() {
        return String.format("Response{success=%b, push=%b, message='%s', data=%s}",
                success, push, message, data);
    }
}