/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 *
 * @author rlaredo
 */
public class ChatServer extends Thread {
    private static final int port = 1900;
    private DataOutputStream dout;
    private Socket socketClient;
    private ServerSocket server;
    private UUID id = UUID.randomUUID();
    private String name;

    public ChatServer() throws IOException {
        this.server = new ServerSocket(port);
        this.start();
    }

    @Override
    public void run() {
        try {
            System.out.println("Esperando conexión...");
            this.socketClient = server.accept();
            this.dout = new DataOutputStream(socketClient.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            if (dout != null) {
                String message = id + "|" + mensaje + System.lineSeparator();
                dout.write(message.getBytes("UTF-8"));
                dout.flush();
            } else {
                System.out.println("Error: El cliente aún no se ha conectado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enviarHello(){
        try {
            if (server != null) {
                if (dout != null) {
                    dout.writeBoolean(true);
                    dout.flush();
                } else {
                    System.out.println("Cliente no conectado");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}