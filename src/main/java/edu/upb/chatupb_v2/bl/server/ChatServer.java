package edu.upb.chatupb_v2.bl.server;

import lombok.Setter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class ChatServer extends Thread {
    private static final int port = 1900;
    private DataOutputStream dout;
    private final ServerSocket server;
    private final UUID id = UUID.randomUUID();
    private String name;
    @Setter
    private SocketClient.SocketListener uiListener;
    SocketClient socketClient;

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

                if (uiListener != null) {
                    this.socketClient.setListener(name, id.toString(), uiListener);
                }

                Controller.getInstance().addClients(socketClient);

                this.socketClient.start();
            } catch (IOException io) {
                System.out.println(getAllStackTraces());
            }
        }
    }

}