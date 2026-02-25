package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.JUi;
import edu.upb.chatupb_v2.repository.BlacklistDao;
import edu.upb.chatupb_v2.repository.Contact;
import edu.upb.chatupb_v2.repository.ContactDao;
import edu.upb.chatupb_v2.repository.Message;
import edu.upb.chatupb_v2.repository.MessageDAO;
import edu.upb.chatupb_v2.repository.comands.*;
import edu.upb.chatupb_v2.repository.enums.TypeMessage;
import lombok.Getter;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Mediator {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    @Getter
    private Map<String, JUi> uis = new HashMap<>();
    private final BlacklistDao blacklistDao = new BlacklistDao();
    private final ContactDao contactDao = new ContactDao();
    private final Set<String> blacklistedUsers = new HashSet<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Mediator instance;

    private Mediator() {
        blacklistedUsers.addAll(blacklistDao.findAllIds());
    }

    public static Mediator getInstance() {
        if (instance == null) instance = new Mediator();
        return instance;
    }

    public void addClients(SocketClient client) {
        clients.putIfAbsent(client.getUID(), client);
    }

    public void delClients(String idUser) {
        clients.remove(idUser);
    }

    public void addUi(JUi ui) {
        uis.putIfAbsent(ui.getUserId().toString(), ui);
    }

    public void delUi(String idUi) {
        uis.remove(idUi);
    }

    public void onInvitationReceived(Invitation invitation, String clientId) {
        for (JUi view : uis.values()) {
            boolean autoRejected = isBlacklisted(invitation.getIdUser());
            boolean accepted = !autoRejected && view.showInvitationDialog(invitation.getUserName(), invitation.getIdUser());

            SocketClient pendingClient = null;
            if (!view.getChatService().getPendingClients().isEmpty()) {
                pendingClient = view.getChatService().getPendingClients().getFirst();
                pendingClient.setUid(invitation.getIdUser());
                Mediator.getInstance().addClients(pendingClient);
                view.getChatService().getPendingClients().removeFirst();
            }

            if (accepted) {
                saveContact(invitation.getIdUser(), invitation.getUserName(), pendingClient);
                Accept acp = new Accept(view.getUserId().toString(), view.getUsername());
                try {
                    SocketClient sc = Mediator.getInstance().getClients().get(invitation.getIdUser());
                    if (sc != null) {
                        sc.send(acp.createFormat());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                addToBlacklist(invitation.getIdUser());
                Decline dec = new Decline();
                try {
                    SocketClient sc = clients.get(clientId);
                    if (sc == null) sc = clients.get(invitation.getIdUser());
                    if (sc == null) sc = pendingClient;
                    if (sc != null) {
                        sc.send(dec.createFormat());
                        sc.close();
                    }
                    clients.remove(invitation.getIdUser());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (autoRejected) {
                    SwingUtilities.invokeLater(() -> view.showMessage("lista negra: " + invitation.getUserName()));
                }
            }
        }
    }

    private boolean isBlacklisted(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        if (blacklistedUsers.contains(userId)) {
            return true;
        }
        boolean exists = blacklistDao.exists(userId);
        if (exists) {
            blacklistedUsers.add(userId);
        }
        return exists;
    }

    private void addToBlacklist(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        blacklistedUsers.add(userId);
        blacklistDao.save(userId);
    }

    public void onAcceptReceived(Accept accept, String clientId) {
        SocketClient sc = clients.get(clientId);
        if (sc == null) {
            for (JUi view : uis.values()) {
                for (SocketClient candidate : clients.values()) {
                    if (view.getUserId().toString().equals(candidate.getUID())) {
                        sc = candidate;
                        break;
                    }
                }
                if (sc != null) {
                    clients.remove(sc.getUID());
                    sc.setUid(clientId);
                    clients.put(clientId, sc);
                    break;
                }
            }
        }
        saveContact(accept.getIdUser(), accept.getUserName(), sc);
        for (JUi view : uis.values())
            SwingUtilities.invokeLater(() -> {
                view.updateStatus("Online");
                view.showMessage("Conexion Aceptada");
            });
    }

    public void onDeclineReceived(Decline decline, String clientId) {
        for (JUi view : uis.values())
            SwingUtilities.invokeLater(() -> {
                view.updateStatus("offline");
                view.showMessage("Conexion Rechazada");
                SocketClient declinedClient = clients.get(clientId);
                if (declinedClient != null) {
                    Mediator.getInstance().delClients(declinedClient.getUID());
                    declinedClient.close();
                }
            });
    }

    public void onHelloReceived(Hello hello, String clientId) {
        for (JUi view : uis.values()) {
            AcceptHello acceptHello = new AcceptHello(view.getUserId().toString());
            SocketClient client = Mediator.getInstance().getClients().get(hello.getIdUser());
            if (client != null) {
                try {
                    client.send(acceptHello.createFormat());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void onChatReceived(Chat chat, String clientId) {
        for (JUi view : uis.values()) {
            if (view.getChatService().isOnline()) {
                System.out.println("Mensaje: " + chat.getMessage());
                SocketClient sc = Mediator.getInstance().getClients().get(chat.getIdUser());
                saveIncomingMessage(chat, view.getUserId().toString(), sc != null ? sc.getIp() : null);
                String name = "Desconocido";
                if (sc != null && sc.getNombre() != null) {
                    name = sc.getNombre();
                }
                String finalName = name;
                SwingUtilities.invokeLater(() -> view.addChatMessage(chat.getMessage(), false, finalName));
                ConfirmRecived confirmRecived = new ConfirmRecived(chat.getIdMessage());
                for (SocketClient client : Mediator.getInstance().getClients().values()) {
                    try {
                        client.send(confirmRecived.createFormat());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    public void onBuzzingReceived(Buzzing buzzing, String clientId) {
        for (JUi view : uis.values()) {
            String name = "Desconocido";
            SocketClient sc = Mediator.getInstance().getClients().get(buzzing.getIdUser());
            if (sc != null) {
                name = sc.getNombre();
            }

            String finalName = name;
            SwingUtilities.invokeLater(() -> view.showBuzzNotification(finalName));
        }
    }

    public void onGoodByeReceived(GoodBye goodBye, String clientId) {
        for (JUi view : uis.values()) {
            String name = "Desconocido";
            SocketClient sc = Mediator.getInstance().getClients().get(goodBye.getIdUser());
            if (sc != null) {
                name = sc.getNombre();
            }

            String finalName = name;
            SwingUtilities.invokeLater(() -> view.showClientOffline(finalName));
        }
    }

    private void saveContact(String userId, String userName, SocketClient sc) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String ip = sc != null ? sc.getIp() : "";
        String name = userName != null && !userName.isBlank() ? userName : "Desconocido";
        try {
            if (contactDao.existByCode(userId)) {
                String query = "UPDATE contact SET name='" + name + "', ip='" + ip + "' WHERE code='" + userId + "'";
                contactDao.update(query);
            } else {
                Contact contact = Contact.builder()
                        .code(userId)
                        .name(name)
                        .ip(ip)
                        .build();
                contactDao.save(contact);
            }
        } catch (Exception e) {
            System.out.println("No se pudo guardar contacto: " + e.getMessage());
        }
        for (JUi view : uis.values()) {
            SwingUtilities.invokeLater(view::reloadContacts);
        }
    }

    private void saveIncomingMessage(Chat chat, String recipientCode, String roomCode) {
        if (chat == null || chat.getMessage() == null || chat.getMessage().isBlank()) {
            return;
        }
        Message message = Message.builder()
                .codMessage(chat.getIdMessage())
                .senderCode(chat.getIdUser())
                .recipientCode(recipientCode)
                .message(chat.getMessage())
                .type(TypeMessage.TEXT)
                .createdDate(LocalDateTime.now().format(DATE_FORMAT))
                .roomCode(roomCode)
                .build();
        try {
            new MessageDAO().save(message);
        } catch (Exception e) {
            System.out.println("No se pudo guardar mensaje recibido: " + e.getMessage());
        }
    }

    public void onAcceptHelloReceived(AcceptHello acceptHello) {}
    public void onDeclineHelloReceived(DeclineHello declineHello) {}
    public void onConfirmedReceived(ConfirmRecived confirmRecived) {}
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {}
    public void onPinMessageReceived(PinMessage pinMessage) {}
    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {}
    public void onThemeReceived(Theme theme) {}
}
