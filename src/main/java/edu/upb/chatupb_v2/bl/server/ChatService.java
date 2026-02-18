package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.repository.comands.*;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class ChatService implements SocketClient.SocketListener{
    private final IChatView view;
    private final String username;
    private final String userId;
    private SocketClient socketClient;
    private ServerSocket serverSocket;
    private Thread helloThread;
    private boolean isRunning = true;

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
                    newClient.setListener(username, userId, this);
                    Controller.getInstance().addClients(newClient);
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
                socketClient.setListener(username, userId, this);
                Controller.getInstance().addClients(socketClient);
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
            Chat chat = new Chat(this.userId, UUID.randomUUID().toString(), messageText);
            for (SocketClient sc : Controller.getInstance().getClients().values()) {
                sc.send(chat.createFormat());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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




    @Override
    public void onInvitationReceived(Invitation invitation) {
        boolean accepted = view.showInvitationDialog(invitation.getUserName(), invitation.getIdUser());

        if (accepted) {
            Accept acp = new Accept(userId, username);
            try {
                SocketClient sc = Controller.getInstance().getClients().get(invitation.getIdUser());
                    if (sc != null) {
                        sc.send(acp.createFormat());
                    }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Decline dec = new Decline();
            try {
                if(socketClient != null) socketClient.send(dec.createFormat());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onAcceptReceived(Accept accept) {
        SwingUtilities.invokeLater(() -> {
            view.updateStatus("Status: Online");
            view.showMessage("Conexión Aceptada");
        });
    }

    @Override
    public void onDeclineReceived(Decline decline) {
        SwingUtilities.invokeLater(() -> {
            view.updateStatus("Status: Rejected");
            view.showMessage("Conexión Rechazada");
            Controller.getInstance().delClients(socketClient.getUID());
            if(socketClient != null) socketClient.close();
        });
    }

    @Override
    public void onHelloReceived(Hello hello) {
        AcceptHello acceptHello = new AcceptHello(userId);
        SocketClient client = Controller.getInstance().getClients().get(hello.getIdUser());
            if (client != null) {
                try {
                    client.send(acceptHello.createFormat());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }

    }

    @Override
    public void onChatReceived(Chat chat) {
        System.out.println("Mensaje: " + chat.getMessage());
        ConfirmRecived confirmRecived = new ConfirmRecived(chat.getIdMessage());
        for (SocketClient client : Controller.getInstance().getClients().values()) {
            try {
                client.send(confirmRecived.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    @Override
    public void onBuzzingReceived(Buzzing buzzing) {
        String name = "Desconocido";
        SocketClient sc = Controller.getInstance().getClients().get(buzzing.getIdUser());
            if (sc != null) {
                name = sc.getNombre();

            }

        String finalName = name;
        SwingUtilities.invokeLater(() -> view.showBuzzNotification(finalName));
    }

    @Override public void onAcceptHelloReceived(AcceptHello acceptHello) {}
    @Override public void onDeclineHelloReceived(DeclineHello declineHello) {}
    @Override public void onConfirmedReceived(ConfirmRecived confirmRecived) {}
    @Override public void onDeleteMessageReceived(DeleteMessage deleteMessage) {}
    @Override public void onPinMessageReceived(PinMessage pinMessage) {}
    @Override public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {}
    @Override public void onThemeReceived(Theme theme) {}
}
