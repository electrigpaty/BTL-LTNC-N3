package com.auction.server.net;

import com.auction.common.dto.Request;
import com.auction.common.dto.Response;
import com.auction.server.observer.ClientObserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final String clientId;
    private final Socket socket;
    private final RequestRouter router;
    private final ClientObserver observer;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, RequestRouter router) {
        this(UUID.randomUUID().toString(), socket, router);
    }

    public ClientHandler(String clientId, Socket socket, RequestRouter router) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client id must not be blank.");
        }
        this.clientId = clientId;
        this.socket = Objects.requireNonNull(socket, "Socket must not be null.");
        this.router = Objects.requireNonNull(router, "RequestRouter must not be null.");
        this.observer = new ClientObserver(this.clientId, status -> sendResponse(Response.push(status)));
    }

    public String getClientId() { return clientId; }

    public ClientObserver getObserver() { return observer; }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (running && !socket.isClosed()) {
                Object raw = in.readObject();
                if (!(raw instanceof Request request)) {
                    sendResponse(Response.error("Gói tin không hợp lệ."));
                    continue;
                }
                Response response = router.route(request, this);
                if (response != null) {
                    sendResponse(response);
                }
            }
        } catch (EOFException e) {
            // Client đóng kết nối bình thường
        } catch (IOException | ClassNotFoundException e) {
            // Kết nối bị ngắt đột ngột
        } finally {
            close();
        }
    }

    public synchronized void sendResponse(Response response) {
        if (out == null || socket.isClosed()) {
            throw new IllegalStateException("Kết nối tới client đã đóng.");
        }
        try {
            out.writeObject(response);
            out.reset();
            out.flush();
        } catch (IOException e) {
            close();
            throw new IllegalStateException("Lỗi gửi dữ liệu tới client: " + clientId, e);
        }
    }

    public void close() {
        running = false;
        router.onClientDisconnected(this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}