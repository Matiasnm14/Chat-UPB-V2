package edu.upb.chatupb_v2.model.network;

import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.comands.*;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;
import edu.upb.chatupb_v2.view.IChatView;
import edu.upb.chatupb_v2.view.JUi;
import lombok.Setter;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatServer extends Thread {
    private final String username;
    private final String userId;
    private SocketClient socketClient;
    private ServerSocket serverSocket;
    private Thread helloThread;
    private boolean isRunning = true;


    public ChatServer(String username, String userId) {
        this.username = username;
        this.userId = userId;
        startServer();
        startHelloService();
    }
    //ALGO
//    @Override
//    public void run() {
//        while (true) {
//            try {
//                Socket socket = server.accept();
//                System.out.println("Conexión entrante aceptada.");
//
//                this.socketClient = new SocketClient(socket);
//
//                if (uiListener != null) {
//                    this.socketClient.setListener(name, id.toString(), uiListener);
//                }
//
//                this.socketClient.start();
//            } catch (IOException io) {
//                System.out.println(getAllStackTraces());
//            }
//        }
//    }
    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(1900);
                System.out.println("Servidor escuchando en puerto 1900...");

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    SocketClient newClient = new SocketClient(clientSocket);

                    newClient.setListener(username, userId, Controller.getInstance());
                    Controller.getInstance().getPendingClients().add(newClient);
                    newClient.start();
                    System.out.println("Nuevo cliente conectado desde: " + clientSocket.getInetAddress());
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        Controller.getInstance().getUis().get(userId).
                                showError("No se pudo iniciar el servidor (¿Puerto 1900 ocupado?): " + e.getMessage())
                );
            }
        }).start();
    }
    public void connect(String ip) {
        new Thread(() -> {
            try {
                socketClient = new SocketClient(ip);
                socketClient.setListener(username, userId, Controller.getInstance());

                socketClient.start();

                Invitation myInvite = new Invitation(userId, username);
                socketClient.send(myInvite.createFormat());

                Controller.getInstance().getPendingClients().add(socketClient);

                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(userId).
                    updateStatus("Status: Enviando invitación..."));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(userId)
                        .showError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }

    public void sendMessage(String messageText, String destinationId) {
        if (destinationId == null || destinationId.trim().isEmpty()) {
            SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(userId).
                    showError("Selecciona un contacto primero."));
            return;
        }

        try {
            Chat chat = new Chat(this.userId, UUID.randomUUID().toString(), messageText);

            Message msgDb = new Message(
                    chat.getIdMessage(),
                    destinationId,
                    messageText,
                    TypeMessage.TEXT,
                    StatusMessage.SENT,
                    LocalDate.now().toString()
            );
            MessageDAO.getInstance().save(msgDb);

            SocketClient sc = Controller.getInstance().getClients().get(destinationId);

            if (sc != null) {
                sc.send(chat.createFormat());
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(userId).
                        showMessage("Tú | " + messageText));
            } else {
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(userId).
                        showError("El contacto no está en línea en este momento, pero el mensaje se guardó."));
            }

        } catch (Exception e) {
            System.out.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    public void sendBuzz() {
        for (SocketClient sc : Controller.getInstance().getClients().values()) {
            Buzzing bz = new Buzzing(this.userId);
            try {
                sc.send(bz.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void sendBye(){
        for (SocketClient sc : Controller.getInstance().getClients().values()) {
            Bye bye = new Bye(this.userId);
            try {
                sc.send(bye.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void startHelloService() {
        helloThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(5000);
                    for (SocketClient client : Controller.getInstance().getClients().values()) {
                        Hello hello = new Hello(userId);
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

}