package edu.upb.chatupb_v2.bl.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class ChatServer extends Thread {
    private int port = 1900;
    private DataOutputStream dout;
    private Socket socket;
    private ServerSocket server;
    private UUID id = UUID.randomUUID();
    private String name;
    private SocketClient.SocketListener uiListener;
    SocketClient socketClient;
    // constructor con puerto (si está ocupado, sube puerto automáticamente)
    public ChatServer(int port, SocketClient.SocketListener uiListener) throws IOException {
        this.uiListener = uiListener;
        int p = port;
        while (true) {
            try {
                this.port = p;
                this.server = new ServerSocket(this.port);
                break;
            } catch (java.net.BindException ex) {
                p++;
            }
        }

        this.start();
        System.out.println("Servidor escuchando en puerto: " + this.port);
    }

    public ChatServer(SocketClient.SocketListener uiListener) throws IOException {
        this(1900, uiListener);
    }
    public ChatServer() throws IOException {
        this.server = new ServerSocket(this.port);
        this.start();
        System.out.println("Servidor escuchando en puerto: " + this.port);
    }

    @Override
    public void run() {
        try {
            Socket socket = server.accept();
            System.out.println("Conexión entrante aceptada en puerto " + this.port);

            this.socketClient = new SocketClient(socket);

            if (uiListener != null) {
                this.socketClient.setListener(uiListener);
            }

            this.socketClient.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje) {
        if (this.socketClient != null) {
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

    public void enviarHello() {
        try {
            if (server != null) {
                if (dout != null) {
                    dout.writeBoolean(true);
                    dout.flush();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
