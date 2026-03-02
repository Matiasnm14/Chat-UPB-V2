package edu.upb.chatupb_v2.Model.network;

import edu.upb.chatupb_v2.Controller.ClientController;
import edu.upb.chatupb_v2.Controller.UIController;
import edu.upb.chatupb_v2.Model.entities.User;
import edu.upb.chatupb_v2.Model.entities.comands.Hello;
import edu.upb.chatupb_v2.Model.entities.comands.Invitation;
import edu.upb.chatupb_v2.Model.factory.SocketListener;
import lombok.Setter;

import javax.swing.*;
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

    private boolean isRunning = true;
    @Setter
    private UIController uiListener;
    SocketClient socketClient;

    public ChatServer() throws IOException {
        this.server = new ServerSocket(port);
        this.startHelloService();
        this.start();
    }

    private void startHelloService() {
        Thread helloThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(5000);
                    for (SocketClient client : ClientController.getInstance().getClients().values()) {
                        Hello hello = new Hello(id.toString());
                        try {
                            client.send(hello.createFormat());
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        helloThread.start();
    }

    //ALGO
    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Conexión entrante aceptada.");

                this.socketClient = new SocketClient(socket);
                this.socketClient.setSocketListener(uiListener);

                this.socketClient.start();
            } catch (IOException io) {
                System.out.println(getAllStackTraces());
            }
        }
    }

}