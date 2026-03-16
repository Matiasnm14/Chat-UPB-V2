package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Model.entities.*;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.entities.enums.*;
import edu.upb.chatupb_v2.Model.factory.*;
import edu.upb.chatupb_v2.Model.network.*;
import edu.upb.chatupb_v2.Model.repository.MessageDAO;
import edu.upb.chatupb_v2.Model.repository.UserDAO;
import edu.upb.chatupb_v2.VIews.IChatView;
import lombok.Getter;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

public class UIController implements SocketListener {
    private final IChatView view;
    private final String username;
    @Getter
    private String userId;
    private SocketClient socketClient;

    public UIController(IChatView view, String username, String userId) {
        this.view = view;
        this.username = username;
        this.userId = userId;
    }

    // UIController - solo orquesta, no toca sockets directamente
    public void connect(String ip) {
        new Thread(() -> {
            try {
                ClientController.getInstance().connectTo(ip, userId, username, this);
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

    public void deleteUser(User user){
        try {
            UserDAO.getInstance().deleteUser(user.getId());
            MessageDAO.getInstance().deleteConversation(userId, user.getId());
        } catch (ConnectException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    TextAnalizeController textAnalizeController = new TextAnalizeController();
    public void sendMessage(String messageText, User target) {
        try {
            messageText = textAnalizeController.analizarTexto(messageText);
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
            ClientController.getInstance().sendToClient(target.getId(), target.getIp(), chat, this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessageUnique(String messageText, User target) {
        try {
            messageText = textAnalizeController.analizarTexto(messageText);
            UniqueMessage uniqueMessage = new UniqueMessage(this.userId, UUID.randomUUID().toString(), messageText);
            MessageDAO.getInstance().save(new Message(
                    uniqueMessage.getIdMessage(),
                    this.userId,
                    target.getId(),
                    "Mensaje Único",
                    TypeMessage.TEXT,
                    StatusMessage.SENT,
                    LocalDate.now().toString()
            ));
            ClientController.getInstance().sendToClientUnique(target.getId(), target.getIp(), uniqueMessage, this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendImage(File file, User target) {
        try {
            byte[] imageBytes = Files.readAllBytes(file.toPath());
            String messageBase = Base64.getEncoder().encodeToString(imageBytes);
            Image image = new Image(this.userId, UUID.randomUUID().toString(), messageBase);
            MessageDAO.getInstance().save(new Message(
                    image.getIdMessage(),
                    this.userId,
                    target.getId(),
                    messageBase,
                    TypeMessage.IMAGE,
                    StatusMessage.SENT,
                    LocalDate.now().toString()
            ));
            ClientController.getInstance().sendToClient(target.getId(), target.getIp(), image, this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendContact(User user) throws IOException {
        ClientController.getInstance().senda(user);
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

            SwingUtilities.invokeLater(() ->
                    view.onNewConnectionEstablished(invitation.getUserName(), invitation.getIdUser())
            );

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
        SwingUtilities.invokeLater(() ->
                view.onNewConnectionEstablished(accept.getUserName(), client.getUID())
        );
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
                SwingUtilities.invokeLater(view::renderContacts);
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
        SwingUtilities.invokeLater(() ->
                view.onNewConnectionEstablished(client.getNombre(), client.getUID())
        );
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
    public void onNewConnectionEstablished(String userName) {
        view.renderContacts();
        view.updateStatus("Status: Online");
    }

    @Override
    public void onNewFriendReceived(NewFriend newFriend) {
        try {
            UserDAO.getInstance().save(new User("0000001",newFriend.getName_user(), newFriend.getIp_user(), userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onImageReceived(Image image) {
        view.showImage(image);
        try {
            MessageDAO.getInstance().save(new Message(image.getIdMessage(),image.getSendUser(),userId, image.getMessage(), TypeMessage.IMAGE, StatusMessage.READ, LocalDate.now().toString()));
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {
        String id_message = deleteMessage.getIdMessage();
        try {
            MessageDAO.getInstance().delete(id_message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPinMessage(String messageId, User target) {
        PinMessage pin = new PinMessage(messageId);
        SocketClient sc = ClientController.getInstance().getClients().get(target.getId());
        if (sc == null) {
            SwingUtilities.invokeLater(() -> view.showError("No hay conexión activa con " + target.getName()));
            return;
        }
        try {
            sc.send(pin.createFormat());
        } catch (java.io.IOException e) {
            SwingUtilities.invokeLater(() -> view.showError("Error al fijar mensaje: " + e.getMessage()));
        }
    }

    @Override
    public void onPinMessageReceived(PinMessage pinMessage) {
        SwingUtilities.invokeLater(() -> view.showPinnedMessage(pinMessage.getIdMessage()));
    }


    @Override
    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        try {
            MessageDAO.getInstance().save(new Message(
                    uniqueMessage.getIdMessage(),
                    uniqueMessage.getSendUser(),
                    userId,
                    "Mensaje Único",
                    TypeMessage.TEXT,
                    StatusMessage.READ,
                    LocalDate.now().toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() -> view.showUniqueMessage(uniqueMessage));
    }

    public void sendTheme(String themeId, User target) {
        Theme theme = new Theme(this.userId, themeId);
        SocketClient sc = ClientController.getInstance().getClients().get(target.getId());
        if (sc == null) {
            SwingUtilities.invokeLater(() -> view.showError("No hay conexión activa con " + target.getName()));
            return;
        }
        try {
            sc.send(theme.createFormat());
        } catch (java.io.IOException e) {
            SwingUtilities.invokeLater(() -> view.showError("Error al enviar tema: " + e.getMessage()));
        }
    }

    @Override
    public void onThemeReceived(Theme theme) {
        SwingUtilities.invokeLater(() -> view.changeThemeSelected(theme));
    }
}