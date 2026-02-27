package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.controller.Controller;
import lombok.Setter;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class ChatServer extends Thread {
    private static final int port = 1900;
    private final ServerSocket server;
    private final String username;
    private final String userId;

    public ChatServer(String username, String userId) throws IOException {
        this.username = username;
        this.userId = userId;
        this.server = new ServerSocket(port);
        System.out.println("Servidor escuchando en puerto " + port + "...");
    }
    //ALGO
    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Conexión entrante aceptada desde: " + socket.getInetAddress());

                SocketClient newClient = new SocketClient(socket);
                newClient.setListener(username, userId, Controller.getInstance());

                Controller.getInstance().getPendingClients().add(newClient);
                newClient.start();

            } catch (IOException io) {
                System.out.println("Error en el servidor: " + io.getMessage());
            }
        }
    }

}