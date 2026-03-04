package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Model.entities.User;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.entities.enums.StatusMessage;
import edu.upb.chatupb_v2.Model.entities.enums.TypeMessage;
import edu.upb.chatupb_v2.Model.factory.SocketListener;
import edu.upb.chatupb_v2.Model.network.SocketClient;
import edu.upb.chatupb_v2.Model.entities.Message;
import edu.upb.chatupb_v2.Model.repository.MessageDAO;
import edu.upb.chatupb_v2.VIews.IChatView;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

public class UIController implements SocketListener {
    private final IChatView view;
    private SocketClient active;
    private final String username;
    private String userId;
    private SocketClient socketClient;

    public UIController(IChatView view, String username, String userId) {
        this.view = view;
        this.username = username;
        this.userId = userId;
    }

    public String getUserId() { return userId; }

    // UIController - solo orquesta, no toca sockets directamente
    public void connect(String ip) {
        new Thread(() -> {
            try {
                ClientController.getInstance().connectTo(ip, userId, username, this, 1);
                SwingUtilities.invokeLater(() -> view.updateStatus("Status: Enviando invitación..."));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> view.showError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }

    public void connectPrev(User user) {
        new Thread(() -> {
            try {
                ClientController.getInstance().connectToPrevious(user.getIp(), userId, this);
                SwingUtilities.invokeLater(() -> view.updateStatus("Status: Enviando Hello..."));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> view.showError("Error de conexión: " + e.getMessage()));
            }
        }).start();
    }

    //CONNECTFORHELLOS: ESTA MISMA HACE QUE SE HAGA FETCH DE LA BASE DE DATOS PARA CONSEGUIR LAS IPS, CONECTARSE Y MANDAR UN HELLO EN VEZ DE UN INVITATION
    public void sendMessage(String messageText, User target) {
        try {
            Chat chat = new Chat(this.userId, UUID.randomUUID().toString(), messageText);
            MessageDAO.getInstance().save(new Message(
                    chat.getIdMessage(),
                    this.userId,
                    target.getId(),
                    messageText,
                    TypeMessage.TEXT,
                    StatusMessage.SENT,
                    LocalDate.now().toString()
            ));
            ClientController.getInstance().sendToClient(target.getId(), target.getIp(), chat.createFormat(), this);
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

    public void sendBye() {
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
    public void onInvitationReceived(Invitation invitation, SocketClient client) {
        System.out.println("Invitation idUser: " + invitation.getIdUser());
        System.out.println("Client UID: " + client.getUID());
        System.out.println("Client nombre: " + client.getNombre());
        ClientController.getInstance().registerClient(client);
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
    public void onAcceptReceived(Accept accept, SocketClient client) {
        ClientController.getInstance().registerClient(client);
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
            if (socketClient != null) socketClient.close();
        });
    }

    @Override
    public void onHelloReceived(Hello hello, SocketClient client) {
        Command response;
        if (ClientController.getInstance().userInDB(hello.getIdUser())) {
            // Usuario ya conocido: aceptar y actualizar socket activo
            ClientController.getInstance().registerClient(client);
            try {
                System.out.println("HELLO ACCEPTED (known user)!");
                response = new AcceptHello(userId);
                client.send(response.createFormat());
                SwingUtilities.invokeLater(() -> view.renderContacts());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Usuario desconocido: rechazar
            try {
                System.out.println("HELLO DECLINED (unknown user)!");
                response = new DeclineHello();
                client.send(response.createFormat());
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onChatReceived(Chat chat) {
        view.showChat(chat);
        System.out.println(chat.getMessage());
        try {
            MessageDAO.getInstance().save(new Message(
                    chat.getIdMessage(),
                    chat.getSendUser(),
                    userId,
                    chat.getMessage(),
                    TypeMessage.TEXT,
                    StatusMessage.READ,
                    LocalDate.now().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ConfirmRecived confirmRecived = new ConfirmRecived(chat.getIdMessage());
        SocketClient client = ClientController.getInstance().getClients().get(chat.getSendUser());
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

    @Override
    public void onByeReceived(Bye bye) {
        String id = bye.getIdUser();
        System.out.println("ID: " + id);
        SocketClient sc = ClientController.getInstance().getClients().get(id);
        if (sc != null) {
            sc.close();
        }
        SwingUtilities.invokeLater(() -> view.showByeNotification(id));
    }

    @Override
    public void onAcceptHelloReceived(AcceptHello acceptHello, SocketClient client) {
        client.setUid(acceptHello.getIdUser());
        ClientController.getInstance().registerClient(client);
        try {
            ClientController.getInstance().flushPending(client); // usa el socket como clave
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() -> {
            view.renderContacts();
            view.updateStatus("Status: Online");
        });
    }

    @Override
    public void onDeclineHelloReceived(DeclineHello declineHello, SocketClient client) {
        client.close();
    }

    @Override
    public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        System.out.println("Recibido");
        try {
            MessageDAO.getInstance().updateMessage(confirmRecived.getIdMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public void onPinMessageReceived(PinMessage pinMessage) {
    }

    @Override
    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        System.out.println("MENSAJE ÚNICO");
    }

    @Override
    public void onThemeReceived(Theme theme) {
    }
}