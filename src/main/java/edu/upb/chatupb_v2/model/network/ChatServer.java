package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.controller.Mediator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer extends Thread {
    private static final int port = 1900;
    private final ServerSocket server;
    private SocketClient socketClient;

    public ChatServer() throws IOException {
        this.server = new ServerSocket(port);
        this.start();
    }
    //ALGO
    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Conexión entrante aceptada.");

                this.socketClient = new SocketClient(socket);

                Mediator.getInstance().addPendingClient(socketClient);

                this.socketClient.start();
            } catch (IOException io) {
                System.out.println(getAllStackTraces());
            }
        }
    }

}
