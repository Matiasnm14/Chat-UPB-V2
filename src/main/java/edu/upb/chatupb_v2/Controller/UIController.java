package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Model.entities.*;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.entities.enums.*;
import edu.upb.chatupb_v2.Model.factory.SocketListener;
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

/**
 * Controlador principal de la UI. Orquesta las acciones del usuario,
 * delega la comunicación de red en {@link ClientController} e implementa
 * {@link SocketListener} para procesar los eventos entrantes del socket.
 */
public class UIController implements SocketListener {

    // =========================================================================
    // ESTADO
    // =========================================================================

    private final IChatView view;
    private final String username;

    @Getter
    private final String userId;

    private SocketClient socketClient;

    private final TextAnalizeController textAnalizeController = new TextAnalizeController();

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public UIController(IChatView view, String username, String userId) {
        this.view     = view;
        this.username = username;
        this.userId   = userId;
    }

    // =========================================================================
    // ACCIONES DE CONEXIÓN
    // =========================================================================

    /** Inicia una nueva conexión con el servidor en la IP indicada. */
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

    /** Reconecta con un usuario previamente conocido. */
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

    // =========================================================================
    // ACCIONES DE USUARIO Y MENSAJES
    // =========================================================================

    /** Elimina un usuario y su conversación de la base de datos. */
    public void deleteUser(User user) {
        try {
            UserDAO.getInstance().deleteUser(user.getId());
            MessageDAO.getInstance().deleteConversation(userId, user.getId());
        } catch (ConnectException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void blockUser(User user){
        ClientController.getInstance().bloquear(user.getId());
    }
    public void unblockUser(User user){
        ClientController.getInstance().desbloquear(user.getId());
    }

    /** Elimina un mensaje de la base de datos por su ID. */
    public void deleteMessage(String id) {
        try {
            MessageDAO.getInstance().delete(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Analiza, persiste y envía un mensaje de texto al destinatario. */
    public void sendMessage(String messageText, User target, String idMessage) {
        try {
            messageText = textAnalizeController.analizarTexto(messageText);
            Chat chat = new Chat(this.userId, idMessage, messageText);
            saveMessage(idMessage, target.getId(), messageText, TypeMessage.TEXT);
            ClientController.getInstance().sendToClient(target.getId(), target.getIp(), chat, this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /** Envía un mensaje único (de un solo uso) al destinatario. */
    public void sendMessageUnique(String messageText, User target, String idMessage) {
        try {
            messageText = textAnalizeController.analizarTexto(messageText);
            UniqueMessage uniqueMessage = new UniqueMessage(this.userId, idMessage, messageText);
            saveMessage(idMessage, target.getId(), "Mensaje Único", TypeMessage.TEXT);
            ClientController.getInstance().sendToClientUnique(target.getId(), target.getIp(), uniqueMessage, this);
            view.addMessage(messageText, true, idMessage,true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /** Codifica una imagen en Base64, la persiste y la envía. */
    public void sendImage(File file, User target) {
        try {
            String messageBase = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            Image image = new Image(this.userId, UUID.randomUUID().toString(), messageBase);
            saveMessage(image.getIdMessage(), target.getId(), messageBase, TypeMessage.IMAGE);
            ClientController.getInstance().sendToClient(target.getId(), target.getIp(), image, this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /** Envía la información de contacto de un usuario. */
    public void sendContact(User user) throws IOException {
        ClientController.getInstance().senda(user);
    }

    /** Envía un buzz al contacto. */
    public void sendBuzz() {
        Buzzing bz = new Buzzing(this.userId);
        broadcastToAll(bz);
    }

    /** Notifica a todos los clientes que el usuario se desconecta. */
    public void sendBye() {
        Bye bye = new Bye(this.userId);
        broadcastToAll(bye);
    }

    /** Envía un comando de fijar mensaje al cliente destino. */
    public void sendPinMessage(String messageId, User target) {
        PinMessage pin = new PinMessage(messageId);
        sendToClient(target, pin, "Error al fijar mensaje: ");
    }

    /** Envía el tema seleccionado al cliente destino. */
    public void sendTheme(String themeId, User target) {
        Theme theme = new Theme(this.userId, themeId);
        sendToClient(target, theme, "Error al enviar tema: ");
    }

    // =========================================================================
    // IMPLEMENTACIÓN SocketListener
    // =========================================================================

    @Override
    public void onInvitationReceived(Invitation invitation, SocketClient client) {
//        System.out.println("Invitation idUser: "  + invitation.getIdUser());
//        System.out.println("Client UID: "         + client.getUID());
//        System.out.println("Client nombre: "      + client.getNombre());

        ClientController.getInstance().registerClient(client);
        boolean accepted = view.showInvitationDialog(invitation.getUserName(), invitation.getIdUser());

        if (accepted) {
            sendCommandToClient(invitation.getIdUser(), new Accept(userId, username));
            SwingUtilities.invokeLater(() ->
                    view.onNewConnectionEstablished(invitation.getUserName(), invitation.getIdUser()));
        } else {
            sendCommandToClient(invitation.getIdUser(), new Decline());
        }
    }

    @Override
    public void onAcceptReceived(Accept accept, SocketClient client) {
        ClientController.getInstance().registerClient(client);
        SwingUtilities.invokeLater(() ->
                view.onNewConnectionEstablished(accept.getUserName(), client.getUID()));
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
        if (ClientController.getInstance().userInDB(hello.getIdUser())) {
            ClientController.getInstance().registerClient(client);
            client.setName(ClientController.getInstance().retrieveName(hello.getIdUser()));
            System.out.println("HELLO ACCEPTED (known user)!");
            sendCommandViaSocket(client, new AcceptHello(userId));
            SwingUtilities.invokeLater(view::renderContacts);
        } else {
            System.out.println("HELLO DECLINED (unknown user)!");
            sendCommandViaSocket(client, new DeclineHello());
            client.close();
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

            SocketClient client = ClientController.getInstance().getClients().get(chat.getSendUser());
            client.send(new ConfirmRecived(chat.getIdMessage()).createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBuzzingReceived(Buzzing buzzing) {
        String name = ClientController.getInstance().retrieveName(buzzing.getIdUser());
        SwingUtilities.invokeLater(() -> view.showBuzzNotification(name));
    }

    @Override
    public void onByeReceived(Bye bye) {
        String id = bye.getIdUser();
        System.out.println("ID: " + id);
        SocketClient sc = ClientController.getInstance().getClients().get(id);
        if (sc != null) sc.close();
        SwingUtilities.invokeLater(() -> view.showByeNotification(id));
    }

    @Override
    public void onAcceptHelloReceived(AcceptHello acceptHello, SocketClient client) {
        client.setUid(acceptHello.getIdUser());
        ClientController.getInstance().registerClient(client);
        client.setName(ClientController.getInstance().retrieveName(acceptHello.getIdUser()));
        try {
            ClientController.getInstance().flushPending(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() ->
                view.onNewConnectionEstablished(client.getNombre(), client.getUID()));
    }

    @Override
    public void onDeclineHelloReceived(DeclineHello declineHello, SocketClient client) {
        client.close();
    }

    @Override
    public void onConfirmedReceived(ConfirmRecived confirmRecived) {
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
            UserDAO.getInstance().save(
                    new User(newFriend.getId_sent(), newFriend.getName_user(), newFriend.getIp_user(), userId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        view.renderContacts();
    }

    @Override
    public void onImageReceived(Image image) {
        view.showImage(image);
        try {
            MessageDAO.getInstance().save(new Message(
                    image.getIdMessage(),
                    image.getSendUser(),
                    userId,
                    image.getMessage(),
                    TypeMessage.IMAGE,
                    StatusMessage.READ,
                    LocalDate.now().toString()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {
        try {
            MessageDAO.getInstance().delete(deleteMessage.getIdMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPinMessageReceived(PinMessage pinMessage) {
        SwingUtilities.invokeLater(() -> view.showPinnedMessage(pinMessage.getIdMessage()));
    }

    @Override
    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {
        view.addMessage(uniqueMessage.getMessage(), false, uniqueMessage.getIdMessage(),true);
        try {
            MessageDAO.getInstance().save(new Message(
                    uniqueMessage.getIdMessage(),
                    uniqueMessage.getSendUser(),
                    userId,
                    "Mensaje Único",
                    TypeMessage.TEXT,
                    StatusMessage.READ,
                    LocalDate.now().toString()));
            new ConfirmRecived(uniqueMessage.getIdMessage()).execute(socketClient);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        SwingUtilities.invokeLater(() -> view.showUniqueMessage(uniqueMessage));
    }

    @Override
    public void onThemeReceived(Theme theme) {
        SwingUtilities.invokeLater(() -> view.changeThemeSelected(theme));
    }

    // =========================================================================
    // MÉTODOS PRIVADOS DE APOYO
    // =========================================================================

    /**
     * Persiste un mensaje saliente en la base de datos.
     */
    private void saveMessage(String id, String targetId, String body, TypeMessage type) throws Exception {
        MessageDAO.getInstance().save(new Message(
                id,
                this.userId,
                targetId,
                body,
                type,
                StatusMessage.SENT,
                LocalDate.now().toString()));
    }

    /**
     * Envía un comando a todos los clientes conectados (broadcast).
     */
    private void broadcastToAll(Command command) {
        for (SocketClient sc : ClientController.getInstance().getClients().values()) {
            try {
                sc.send(command.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Envía un comando a un cliente específico buscado por ID de usuario.
     * Muestra un error en la vista si el cliente no está conectado.
     */
    private void sendToClient(User target, Command command, String errorPrefix) {
        SocketClient sc = ClientController.getInstance().getClients().get(target.getId());
        if (sc == null) {
            SwingUtilities.invokeLater(() ->
                    view.showError("No hay conexión activa con " + target.getName()));
            return;
        }
        try {
            sc.send(command.createFormat());
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> view.showError(errorPrefix + e.getMessage()));
        }
    }

    /**
     * Envía un comando a un cliente buscado por ID de usuario en el mapa de clientes.
     * Lanza {@link RuntimeException} si falla el envío.
     */
    private void sendCommandToClient(String userId, Command command) {
        SocketClient sc = ClientController.getInstance().getClients().get(userId);
        if (sc == null) return;
        try {
            sc.send(command.createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Envía un comando directamente a través de un {@link SocketClient} dado.
     * Lanza {@link RuntimeException} si falla el envío.
     */
    private void sendCommandViaSocket(SocketClient client, Command command) {
        try {
            client.send(command.createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}