package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Controller.exceptions.ChatException;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.factory.SocketListener;
import edu.upb.chatupb_v2.Model.network.SocketClient;
import edu.upb.chatupb_v2.Model.entities.Message;
import edu.upb.chatupb_v2.Model.repository.MessageDAO;
import edu.upb.chatupb_v2.VIews.IChatView;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UIController implements SocketListener {
    private final IChatView view;
    private final String username;
    private String userId;
    private SocketClient socketClient;
    private ServerSocket serverSocket;
    private boolean isRunning = true;
    public UIController(IChatView view, String username, String userId) {
        this.view = view;
        this.username = username;
        this.userId = userId;
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(1900);
                System.out.println("Servidor escuchando en puerto 1900...");

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    SocketClient newClient = new SocketClient(clientSocket);
                    newClient.setSocketListener(this);

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
                socketClient.setSocketListener(this);
                socketClient.start();
            } catch (Exception e) {
//                SwingUtilities.invokeLater(() -> view.showError("Error de conexión: " + e.getMessage()));
                throw new ChatException("FAILED CONNECTION!");
            }
            try {
                Invitation myInvite = new Invitation(userId, username);
                socketClient.send(myInvite.createFormat());

                SwingUtilities.invokeLater(() -> view.updateStatus("Status: Enviando invitación..."));
            } catch (Exception e){
                throw new ChatException("INVITATION NOT SEND CORRECTLY!");
            }
        }).start();
    }
//CONNECTFORHELLOS: ESTA MISMA HACE QUE SE HAGA FETCH DE LA BASE DE DATOS PARA CONSEGUIR LAS IPS, CONECTARSE Y MANDAR UN HELLO EN VEZ DE UN INVITATION
    public void sendMessage(String messageText) {
        try {
            Chat chat = new Chat(this.userId, UUID.randomUUID().toString(), messageText);
            MessageDAO.getInstance().save(new Message(
                    chat.getIdMessage(),
                    this.userId,
                    messageText,
                    LocalDate.now().toString()
            ));
            for (SocketClient sc : ClientController.getInstance().getClients().values()) {
                sc.send(chat.createFormat());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendBuzz() {
        for (SocketClient sc : ClientController.getInstance().getClients().values()) {
            Buzzing bz = new Buzzing(this.userId);
            try {
                sc.send(bz.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void sendBye(){
        for (SocketClient sc : ClientController.getInstance().getClients().values()) {
            Bye bye = new Bye(this.userId);
            try {
                sc.send(bye.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {

        boolean accepted = view.showInvitationDialog(invitation.getUserName(), invitation.getIdUser());

        if (accepted) {
            Accept acp = new Accept(userId, username);
            try {
                SocketClient sc = ClientController.getInstance().getClients().get(invitation.getIdUser());
                if (sc != null)
                        sc.send(acp.createFormat());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Decline dec = new Decline();
            try {
                SocketClient sc = ClientController.getInstance().getClients().get(invitation.getIdUser());
                if (sc != null)
                    sc.send(dec.createFormat());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onAcceptReceived(Accept accept) {
        SwingUtilities.invokeLater(() -> {
            view.renderContacts();
            view.updateStatus("Status: Online");
            view.showMessage("Conexión Aceptada");
        });
    }

    @Override
    public void onDeclineReceived(Decline decline) {
        SwingUtilities.invokeLater(() -> {
            view.updateStatus("Status: Rejected");
            view.showMessage("Conexión Rechazada");
            ClientController.getInstance().delClients(socketClient.getUID());
            if(socketClient != null) socketClient.close();
        });
    }

    @Override
    public void onHelloReceived(Hello hello) {
        AcceptHello acceptHello = new AcceptHello(userId);
        SocketClient client = ClientController.getInstance().getClients().get(hello.getIdUser());
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
        view.showChat(chat);
        System.out.println(chat.getMessage());
        try {
//            MessageDAO.getInstance().save(new Message(
//                    chat.getIdMessage(),
//                    chat.getIdUser(),
//                    chat.getMessage(),
//                    TypeMessage.TEXT,
//                    StatusMessage.READ,
//                    LocalDate.now().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ConfirmRecived confirmRecived = new ConfirmRecived(chat.getIdMessage());
        SocketClient client = ClientController.getInstance().getClients().get(chat.getIdUser());
        try {
            client.send(confirmRecived.createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBuzzingReceived(Buzzing buzzing) {
        String name = "Desconocido";
        SocketClient sc = ClientController.getInstance().getClients().get(buzzing.getIdUser());
        if (sc != null) {
            name = sc.getNombre();
        }
        String finalName = name;
        SwingUtilities.invokeLater(() -> view.showBuzzNotification(finalName));
    }

    @Override public void onByeReceived(Bye bye){
        String id = bye.getIdUser();
        System.out.println("ID: " + id );
        SocketClient sc = ClientController.getInstance().getClients().get(id);
        if (sc != null){
            sc.close();
        }
        SwingUtilities.invokeLater(() -> view.showByeNotification(id));
    }
    @Override public void onAcceptHelloReceived(AcceptHello acceptHello) {}
    @Override public void onDeclineHelloReceived(DeclineHello declineHello) {}
    @Override public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        System.out.println("Recibido");
//        try {
//            MessageDAO.getInstance().updateMessage(confirmRecived.getIdMessage());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
    @Override
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {
//        String id_message = deleteMessage.getIdMessage();
//        try {
//            MessageDAO.getInstance().delete(id_message);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
    @Override public void onPinMessageReceived(PinMessage pinMessage) {}
    @Override public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        System.out.println("MENSAJE ÚNICO");
    }
    @Override public void onThemeReceived(Theme theme) {}
}
