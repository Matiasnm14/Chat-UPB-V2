package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.repository.Message;
import edu.upb.chatupb_v2.repository.MessageDAO;
import edu.upb.chatupb_v2.repository.comands.*;
import edu.upb.chatupb_v2.repository.enums.TypeMessage;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatService {
    private final IChatView view;

    private final String username;
    private final String userId;
    private SocketClient socketClient;
    private ServerSocket serverSocket;
    private Thread helloThread;
    private boolean isRunning = true;
    @Getter
    private List<SocketClient> pendingClients = new ArrayList<>();
    @Getter
    @Setter
    private boolean online = true;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatService(IChatView view, String username, String userId) {
        this.view = view;
        this.username = username;
        this.userId = userId;
        startServer();
        startHelloService();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(1900);
                System.out.println("Servidor escuchando en puerto 1900...");

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    SocketClient newClient = new SocketClient(clientSocket);

                    newClient.setClient(username, userId);
                    pendingClients.add(newClient);
//                    Mediator.getInstance().addClients(newClient);
                    newClient.start();
                    System.out.println("Nuevo cliente conectado desde: " + clientSocket.getInetAddress());
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        view.showError("No se pudo iniciar el servidor (¿Puerto 1900 ocupado?): " + e.getMessage())
                );
            }
        }).start();
    }
    public void connect(String ip) {
        new Thread(() -> {
            try {
                socketClient = new SocketClient(ip);
                socketClient.setClient(username, userId);
//                System.out.println(username);
//                System.out.println(userId);
                Mediator.getInstance().addClients(socketClient);

                System.out.println(Mediator.getInstance().getClients().size());

                socketClient.start();

                Invitation myInvite = new Invitation(userId, username);
                socketClient.send(myInvite.createFormat());

                SwingUtilities.invokeLater(() -> view.updateStatus("Status: Enviando invitación..."));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> view.showError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }

    public void sendMessage(String messageText) {
        try {
            String messageId = UUID.randomUUID().toString();
            Chat chat = new Chat(this.userId, messageId, messageText);
            for (SocketClient sc : Mediator.getInstance().getClients().values()) {
                sc.send(chat.createFormat());
                String recipientCode = sc.getUID() != null ? sc.getUID() : sc.getIp();
                saveMessage(messageId, this.userId, recipientCode, messageText, sc.getIp());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendBuzz() {
        for (SocketClient sc : Mediator.getInstance().getClients().values()) {
            Buzzing bz = new Buzzing(this.userId);
            try {
                sc.send(bz.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void saveMessage(String codMessage, String senderCode, String recipientCode, String content, String roomCode) {
        if (content == null || content.isBlank()) {
            return;
        }
        Message message = Message.builder()
                .codMessage(codMessage)
                .senderCode(senderCode)
                .recipientCode(recipientCode)
                .message(content)
                .type(TypeMessage.TEXT)
                .createdDate(LocalDateTime.now().format(DATE_FORMAT))
                .roomCode(roomCode)
                .build();
        try {
            new MessageDAO().save(message);
        } catch (Exception e) {
            System.out.println("No se pudo guardar mensaje: " + e.getMessage());
        }
    }

    private void startHelloService() {
        helloThread = new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(5000);
                    for (SocketClient client : Mediator.getInstance().getClients().values()) {
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
