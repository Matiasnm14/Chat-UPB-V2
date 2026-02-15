/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.chatupb_v2.bl.server;

import lombok.Getter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

/**
 *
 * @author rlaredo
 */
public class ChatServer extends Thread {
    private static final int port = 1900;
    private DataOutputStream dout;
    private Socket socket;
    private ServerSocket server;
    private UUID id = UUID.randomUUID();
    private String name;
    private SocketClient.SocketListener uiListener;
    SocketClient socketClient;

    public ChatServer(SocketClient.SocketListener uiListener) throws IOException {
        this.uiListener = uiListener;
        server = new ServerSocket(port);
        this.start(); // Inicia el hilo automáticamente al crear
    }

    public void setUiListener(SocketClient.SocketListener uiListener) {
        this.uiListener = uiListener;
    }

    public ChatServer() throws IOException {
//        this.uiListener = socketClient.getListener();
        this.server = new ServerSocket(port);
        this.start();
    }
    //ALGO
    @Override
    public void run() {
        while (true) {
            try {
                // 1. Esperar conexión
                Socket socket = server.accept();
                System.out.println("Conexión entrante aceptada.");

                // 2. Crear el wrapper SocketClient para esta conexión
                this.socketClient = new SocketClient(socket);

                // 3. ¡AQUÍ ESTA LA CLAVE! Asignar el listener de la UI

                if (uiListener != null) {
                    this.socketClient.setListener(id, uiListener);
                }

                // 4. Iniciar el hilo de lectura del cliente
                this.socketClient.start();
            } catch (IOException io) {
                System.out.println(getAllStackTraces());
            }
        }
    }

    public void enviarMensaje(String mensaje) {
        // En lugar de buscar 'dout' aquí, usamos el currentClient
        if (this.socketClient != null) {
            // Asumiendo que quieres enviar un mensaje de chat normal (protocolo 002 por ejemplo)
            // O si tu SocketClient.send envía texto plano, úsalo directo.
            // Ejemplo formateado: "002|TuMensaje"
            String mensajeFormateado = "007|" + mensaje + System.lineSeparator();

            try {
                this.socketClient.send(mensajeFormateado);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: No hay cliente conectado para enviar.");
        }
    }

    public void enviarHello(){
        try {
            if (server != null) {
                if (dout != null) {
                    dout.writeBoolean(true);
                    dout.flush();
                } else {
//                    System.out.println("Cliente no conectado");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}