package com.auction.server.config;

public final class ServerConfig {
    public static final String HOST = "0.0.0.0";
    public static final int PORT = 8888;
    public static final int THREAD_POOL_SIZE = 32;

    // Luật đấu giá & Anti-Sniping (30 giây cuối cộng thêm 60 giây)
    public static final long ANTI_SNIPING_WINDOW_SECONDS = 30L;
    public static final long ANTI_SNIPING_EXTENSION_SECONDS = 60L;
    public static final double DEFAULT_MIN_INCREMENT = 10_000.0;

    private ServerConfig() {
    }
}