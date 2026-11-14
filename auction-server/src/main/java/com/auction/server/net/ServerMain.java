package com.auction.server.net;

import com.auction.server.config.DatabaseConnection;
import com.auction.server.config.ServerConfig;
import com.auction.server.service.AuctionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerMain {
    private final int port;
    private final ExecutorService clientPool;
    private final RequestRouter router;
    private volatile boolean running;
    private ServerSocket serverSocket;

    public ServerMain(int port, AuctionManager auctionManager) {
        this.port = port;
        this.clientPool = Executors.newFixedThreadPool(ServerConfig.THREAD_POOL_SIZE);
        this.router = new RequestRouter(auctionManager);
    }

    public void start() throws IOException {
        running = true;
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            System.out.println("[Server] Đang lắng nghe tại cổng " + port + "...");
            while (running) {
                Socket clientSocket = ss.accept();
                ClientHandler handler = new ClientHandler(clientSocket, router);
                clientPool.submit(handler);
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        clientPool.shutdownNow();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        DatabaseConnection.getInstance().getConnection();
        AuctionManager auctionManager = new AuctionManager();
        ServerMain server = new ServerMain(ServerConfig.PORT, auctionManager);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("[Server] Lỗi khởi chạy server: " + e.getMessage());
        }
    }
}