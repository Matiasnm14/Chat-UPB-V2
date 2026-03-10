package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.comands.*;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.view.IChatView;
import lombok.Getter;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Mediator implements SocketClient.SocketListener {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    @Getter
    private IChatView view;
    private final Deque<SocketClient> pendingClients = new ArrayDeque<>();
    private final ContactDao contactDao = new ContactDao();
    private final Set<String> blacklistedUsers = new HashSet<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Mediator instance;
    private String localUserId;
    private String localUserName;
    private final Map<String, Boolean> helloPresence = new HashMap<>();
    private static final String IP_KEY_PREFIX = "ip:";
    private volatile String lastHelloIp;
    private volatile String activeContactCode;
    private volatile String activeContactIp;
    private final Map<String, java.util.List<String>> pendingReadBySender = new HashMap<>();
    private final Set<String> confirmedMessageIds = new HashSet<>();
    private final Set<String> readMessageIds = new HashSet<>();

    private Mediator() {}

    public static Mediator getInstance() {
        if (instance == null) instance = new Mediator();
        return instance;
    }

    public void addClients(SocketClient client) {
        if (client == null) {
            return;
        }
        String uid = client.getUID();
        if (uid == null || uid.isBlank()) {
            return;
        }
        SocketClient existing = clients.get(uid);
        if (existing != null && existing != client) {
            existing.close();
        }
        clients.put(uid, client);
    }
    //REVISAR
    public void delClients(String idUser) {
        clients.remove(idUser);
    }

    public void addPendingClient(SocketClient client) {
        if (client != null) {
            pendingClients.addLast(client);
        }
    }

    public void setView(IChatView view) {
        this.view = view;
    }

    public void setLocalUser(String userId, String userName) {
        this.localUserId = userId;
        this.localUserName = userName;
    }

    public void setActiveContact(String contactCode, String contactIp) {
        this.activeContactCode = contactCode;
        this.activeContactIp = contactIp;
        flushPendingReadReceipts(contactCode, contactIp);
    }

    public void connectToContact(String contactCode, String contactIp, String userId, String userName) {
        String resolvedIp = resolveContactIp(contactCode, contactIp);
        if (resolvedIp == null || resolvedIp.isBlank()) {
            IChatView view = this.view;
            if (view != null) {
                SwingUtilities.invokeLater(() -> view.showMessage("No hay IP registrada para este contacto."));
            }
            return;
        }
        invitacion(resolvedIp, userId, userName, contactCode);
    }

    private void flushPendingReadReceipts(String contactCode, String contactIp) {
        if (contactCode == null && contactIp == null) {
            return;
        }
        java.util.List<String> pending;
        synchronized (pendingReadBySender) {
            pending = pendingReadBySender.remove(contactCode);
            if (pending == null && contactIp != null) {
                pending = pendingReadBySender.remove(IP_KEY_PREFIX + contactIp);
            }
        }
        if (pending == null || pending.isEmpty()) {
            return;
        }
        SocketClient client = findClientByCodeOrIp(contactCode, contactIp);
        if (client == null) {
            return;
        }
        for (String messageId : pending) {
            sendConfirmReceived(messageId, client);
        }
    }

    private boolean isActiveContact(String contactCode, String contactIp) {
        if (contactCode != null && !contactCode.isBlank() && contactCode.equals(activeContactCode)) {
            return true;
        }
        return contactIp != null && !contactIp.isBlank() && contactIp.equals(activeContactIp);
    }

    private SocketClient findClientByCodeOrIp(String contactCode, String contactIp) {
        if (contactCode != null && !contactCode.isBlank()) {
            SocketClient byCode = clients.get(contactCode);
            if (byCode != null) {
                return byCode;
            }
        }
        if (contactIp != null && !contactIp.isBlank()) {
            for (SocketClient client : clients.values()) {
                if (contactIp.equals(client.getIp())) {
                    return client;
                }
            }
        }
        return null;
    }

    private String resolveContactIp(String contactCode, String contactIp) {
        if (contactIp != null && !contactIp.isBlank()) {
            return contactIp;
        }
        if (contactCode == null || contactCode.isBlank()) {
            return null;
        }
        try {
            AcceptHello.User.Contact contact = contactDao.findByCode(contactCode);
            if (contact != null && contact.getIp() != null && !contact.getIp().isBlank()) {
                return contact.getIp();
            }
        } catch (Exception e) {
            System.out.println("No se pudo resolver IP por ID: " + e.getMessage());
        }
        return null;
    }

    private void updateContactIpIfNeeded(String userId, SocketClient sc) {
        if (userId == null || userId.isBlank() || sc == null) {
            return;
        }
        String ip = sc.getIp();
        if (ip == null || ip.isBlank()) {
            return;
        }
        try {
            AcceptHello.User.Contact existing = contactDao.findByCode(userId);
            if (existing == null) {
                return;
            }
            String currentIp = existing.getIp();
            if (currentIp == null || currentIp.isBlank() || !currentIp.equals(ip)) {
                contactDao.updateIpByCode(userId, ip);
            }
        } catch (Exception e) {
            System.out.println("No se pudo actualizar IP del contacto: " + e.getMessage());
        }
    }

    private void sendConfirmReceived(String messageId, SocketClient client) {
        if (messageId == null || messageId.isBlank() || client == null) {
            return;
        }
        if (confirmedMessageIds.contains(messageId)) {
            return;
        }
        ConfirmRecived confirmRecived = new ConfirmRecived(messageId);
        try {
            client.send(confirmRecived.createFormat());
            confirmedMessageIds.add(messageId);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isContactOnline(String code, String ip) {
        if ((code == null || code.isBlank()) && (ip == null || ip.isBlank())) {
            return false;
        }
        if (code != null && !code.isBlank()) {
            Boolean presence = helloPresence.get(code);
            if (presence != null) {
                return presence;
            }
        }
        if (ip != null && !ip.isBlank()) {
            Boolean presence = helloPresence.get(IP_KEY_PREFIX + ip);
            if (presence != null) {
                return presence;
            }
        }
        if (code != null && !code.isBlank() && clients.containsKey(code)) {
            return true;
        }
        if (ip != null && !ip.isBlank()) {
            for (SocketClient client : clients.values()) {
                if (ip.equals(client.getIp())) {
                    return true;
                }
            }
        }
        return false;
    }

    //REVISAR CHAT SERVER

    public void sendMessage(String messageText, String userId, String messageId, String recipientCode, String recipientIp) {
        try {
            if (messageText == null || messageText.isBlank()) {
                return;
            }
            SocketClient target = findClientByCodeOrIp(recipientCode, recipientIp);
            String resolvedRecipient = recipientCode;
            String resolvedRoomCode = recipientIp;
            if ((resolvedRecipient == null || resolvedRecipient.isBlank()) && target != null) {
                resolvedRecipient = target.getUID();
            }
            if ((resolvedRoomCode == null || resolvedRoomCode.isBlank()) && target != null) {
                resolvedRoomCode = target.getIp();
            }
            saveOutgoingMessage(messageId, userId, resolvedRecipient != null ? resolvedRecipient : "", messageText, resolvedRoomCode);

            if (target == null) {
                return;
            }
            if (localUserId != null && localUserId.equals(target.getUID())) {
                return;
            }
            Chat chat = new Chat(userId, messageId, messageText);
            target.send(chat.createFormat());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendBuzz(String userId) {
        for (SocketClient sc : Mediator.getInstance().getClients().values()) {
            Buzzing bz = new Buzzing(userId);
            try {
                sc.send(bz.createFormat());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
// CHAT SERVER REVISAR

    @Override
    public void onInvitationReceived(Invitation invitation) {
        SwingUtilities.invokeLater(() -> onInvitationReceived(invitation, invitation.getIdUser()));
    }

    @Override
    public void onAcceptReceived(Accept accept) {
        SwingUtilities.invokeLater(() -> onAcceptReceived(accept, accept.getIdUser()));
    }

    @Override
    public void onDeclineReceived(Decline decline) {
        SwingUtilities.invokeLater(() -> onDeclineReceived(decline, decline.getIdUser()));
    }

    @Override
    public void onHelloReceived(Hello hello) {
        onHelloReceived(hello, hello.getIdUser());
    }

    @Override
    public void onChatReceived(Chat chat) {
        SwingUtilities.invokeLater(() -> onChatReceived(chat, chat.getIdUser()));
    }

    @Override
    public void onBuzzingReceived(Buzzing buzzing) {
        SwingUtilities.invokeLater(() -> onBuzzingReceived(buzzing, buzzing.getIdUser()));
    }

    @Override
    public void onContactShared(PasarContacto pasarContacto) {
        handleContactShared(pasarContacto);
    }

    @Override
    public void onGoodByeReceived(GoodBye goodBye) {
        SwingUtilities.invokeLater(() -> onGoodByeReceived(goodBye, goodBye.getIdUser()));
    }

    @Override
    public void onSocketClosed(SocketClient client) {
        handleClientDisconnected(client);
    }

    public void onInvitationReceived(Invitation invitation, String clientId) {
        IChatView view = this.view;
        if (view == null) {
            return;
        }
        boolean autoRejected = isBlacklisted(invitation.getIdUser());
        boolean accepted = !autoRejected && view.showInvitationDialog(invitation.getUserName(), invitation.getIdUser());

        SocketClient pendingClient = pendingClients.pollFirst();
        if (pendingClient != null) {
            pendingClient.setUid(invitation.getIdUser());
            Mediator.getInstance().addClients(pendingClient);
        }

        if (accepted) {
            saveContact(invitation.getIdUser(), invitation.getUserName(), pendingClient);
            String senderId = localUserId != null ? localUserId : "";
            String senderName = localUserName != null ? localUserName : "Desconocido";
            Accept acp = new Accept(senderId, senderName);
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

    private boolean isBlacklisted(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return blacklistedUsers.contains(userId);
    }

    private void addToBlacklist(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        blacklistedUsers.add(userId);
    }

    public void onAcceptReceived(Accept accept, String clientId) {
        SocketClient sc = clients.get(clientId);
        if (sc == null) {
            for (SocketClient candidate : clients.values()) {
                if (localUserId != null && localUserId.equals(candidate.getUID())) {
                    sc = candidate;
                    break;
                }
            }
            if (sc != null) {
                clients.remove(sc.getUID());
                sc.setUid(clientId);
                clients.put(clientId, sc);
            }
        }
        saveContact(accept.getIdUser(), accept.getUserName(), sc);
        updateContactIpIfNeeded(accept.getIdUser(), sc);
        IChatView view = this.view;
        if (view != null) {
            SwingUtilities.invokeLater(() -> {
                view.updateStatus("Online");
                view.showMessage("Conexion Aceptada");
            });
        }
    }

    public void onDeclineReceived(Decline decline, String clientId) {
        IChatView view = this.view;
        if (view != null) {
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
    }

    public void onHelloReceived(Hello hello, String clientId) {
        String senderId = localUserId != null ? localUserId : "";
        boolean accepted = false;
        boolean autoRejected = isBlacklisted(hello.getIdUser());
        SocketClient client = Mediator.getInstance().getClients().get(hello.getIdUser());
        if (client == null) {
            SocketClient pendingClient = pendingClients.pollFirst();
            if (pendingClient != null) {
                pendingClient.setUid(hello.getIdUser());
                Mediator.getInstance().addClients(pendingClient);
                client = pendingClient;
            }
        }

        if (!autoRejected) {
            accepted = true;
        }
        updateContactIpIfNeeded(hello.getIdUser(), client);

        if (client != null) {
            try {
                if (accepted) {
                    AcceptHello acceptHello = new AcceptHello(senderId);
                    client.send(acceptHello.createFormat());
                } else {
                    DeclineHello declineHello = new DeclineHello(senderId);
                    client.send(declineHello.createFormat());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        if (hello.getIdUser() != null && !hello.getIdUser().isBlank()) {
            helloPresence.put(hello.getIdUser(), accepted);
        }
        if (client != null && client.getIp() != null && !client.getIp().isBlank()) {
            helloPresence.put(IP_KEY_PREFIX + client.getIp(), accepted);
        }
        if (!accepted && client != null) {
            clients.remove(hello.getIdUser());
            client.close();
        }
        IChatView view = this.view;
        if (view != null) {
            SwingUtilities.invokeLater(view::refreshContactPresence);
        }
    }

    public void onChatReceived(Chat chat, String clientId) {
        IChatView view = this.view;
        if (view == null) {
            return;
        }
        if (localUserId != null && localUserId.equals(chat.getIdUser())) {
            return;
        }
        System.out.println("Mensaje: " + chat.getMessage());
        SocketClient sc = Mediator.getInstance().getClients().get(chat.getIdUser());
        updateContactIpIfNeeded(chat.getIdUser(), sc);
        String recipientCode = localUserId != null ? localUserId : "";
        saveIncomingMessage(chat, recipientCode, sc != null ? sc.getIp() : null);
        String name = "Desconocido";
        if (sc != null && sc.getNombre() != null) {
            name = sc.getNombre();
        }
        String finalName = name;
        SwingUtilities.invokeLater(() -> view.addChatMessage(chat.getMessage(), false, finalName));
        boolean activeNow = isActiveContact(chat.getIdUser(), sc != null ? sc.getIp() : null);
        if (activeNow) {
            sendConfirmReceived(chat.getIdMessage(), sc);
        } else if (chat.getIdMessage() != null && !chat.getIdMessage().isBlank()) {
            String key = chat.getIdUser() != null && !chat.getIdUser().isBlank()
                    ? chat.getIdUser()
                    : (sc != null && sc.getIp() != null ? IP_KEY_PREFIX + sc.getIp() : null);
            if (key != null) {
                synchronized (pendingReadBySender) {
                    pendingReadBySender.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(chat.getIdMessage());
                }
            }
        }
    }

    public void onBuzzingReceived(Buzzing buzzing, String clientId) {
        IChatView view = this.view;
        if (view == null) {
            return;
        }
        String name = "Desconocido";
        SocketClient sc = Mediator.getInstance().getClients().get(buzzing.getIdUser());
        if (sc != null) {
            name = sc.getNombre();
        }

        String finalName = name;
        SwingUtilities.invokeLater(() -> view.showBuzzNotification(finalName));
    }

    public void onGoodByeReceived(GoodBye goodBye, String clientId) {
        IChatView view = this.view;
        if (view == null) {
            return;
        }
        String name = "Desconocido";
        SocketClient sc = Mediator.getInstance().getClients().get(goodBye.getIdUser());
        if (sc != null) {
            name = sc.getNombre();
        }

        String finalName = name;
        SwingUtilities.invokeLater(() -> view.showClientOffline(finalName));
        if (goodBye.getIdUser() != null && !goodBye.getIdUser().isBlank()) {
            helloPresence.put(goodBye.getIdUser(), false);
        }
        if (sc != null && sc.getIp() != null && !sc.getIp().isBlank()) {
            helloPresence.put(IP_KEY_PREFIX + sc.getIp(), false);
        }
    }

    private void handleClientDisconnected(SocketClient client) {
        if (client == null) {
            return;
        }
        String uid = client.getUID();
        String ip = client.getIp();
        String name = client.getNombre() != null ? client.getNombre() : "Desconocido";
        if (uid != null && !uid.isBlank()) {
            helloPresence.put(uid, false);
            clients.remove(uid);
        } else {
            clients.entrySet().removeIf(entry -> entry.getValue() == client);
        }
        if (ip != null && !ip.isBlank()) {
            helloPresence.put(IP_KEY_PREFIX + ip, false);
        }
        IChatView view = this.view;
        if (view != null) {
            SwingUtilities.invokeLater(() -> {
                view.showClientOffline(name);
                view.refreshContactPresence();
            });
        }
    }

    private void saveContact(String userId, String userName, SocketClient sc) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String ip = sc != null ? sc.getIp() : null;
        String name = userName != null && !userName.isBlank() ? userName : "Desconocido";
        try {
            if (contactDao.existByCode(userId)) {
                AcceptHello.User.Contact existing = contactDao.findByCode(userId);
                String finalIp = (ip != null && !ip.isBlank())
                        ? ip
                        : (existing != null ? existing.getIp() : "");
                String query = "UPDATE contact SET name='" + name + "', ip='" + finalIp + "' WHERE code='" + userId + "'";
                contactDao.update(query);
            } else {
                AcceptHello.User.Contact contact = AcceptHello.User.Contact.builder()
                        .code(userId)
                        .name(name)
                        .ip(ip != null ? ip : "")
                        .build();
                contactDao.save(contact);
            }
        } catch (Exception e) {
            System.out.println("No se pudo guardar contacto: " + e.getMessage());
        }
        IChatView view = this.view;
        if (view != null) {
            SwingUtilities.invokeLater(view::reloadContacts);
        }
    }

    private void saveIncomingMessage(Chat chat, String recipientCode, String roomCode) {
        if (chat == null || chat.getMessage() == null || chat.getMessage().isBlank()) {
            return;
        }
        MessageDAO.Message message = MessageDAO.Message.builder()
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

    private void saveOutgoingMessage(String codMessage, String senderCode, String recipientCode, String content, String roomCode) {
        if (content == null || content.isBlank()) {
            return;
        }
        MessageDAO.Message message = MessageDAO.Message.builder()
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

    public void invitacion (String ip, String userId, String userName){
        invitacion(ip, userId, userName, null);
    }

    public void invitacion (String ip, String userId, String userName, String contactCodeHint){
        SocketClient client;
        try {
            client =new SocketClient(ip);
            client.setClient(userName, userId);
            Mediator.getInstance().addClients(client);
            client.addListener(this);
            client.start();
        }catch (Exception e){
            throw new OperationException("No se logro establecer la conexion");
        }
        boolean knownContact = false;
        AcceptHello.User.Contact knownByIp = null;
        AcceptHello.User.Contact knownByCode = null;
        try {
            if (contactCodeHint != null && !contactCodeHint.isBlank()) {
                knownContact = contactDao.existByCode(contactCodeHint);
                if (knownContact) {
                    knownByCode = contactDao.findByCode(contactCodeHint);
                }
            }
            if (!knownContact) {
                knownContact = contactDao.existByIp(ip);
                if (knownContact) {
                    knownByIp = contactDao.findByIp(ip);
                }
            }
        } catch (Exception e) {
            knownContact = false;
        }
        try {
            lastHelloIp = ip;
            helloPresence.put(IP_KEY_PREFIX + ip, false);
            String codeForPresence = null;
            if (knownByCode != null && knownByCode.getCode() != null && !knownByCode.getCode().isBlank()) {
                codeForPresence = knownByCode.getCode();
            } else if (knownByIp != null && knownByIp.getCode() != null && !knownByIp.getCode().isBlank()) {
                codeForPresence = knownByIp.getCode();
            } else if (contactCodeHint != null && !contactCodeHint.isBlank()) {
                codeForPresence = contactCodeHint;
            }
            if (codeForPresence != null) {
                helloPresence.put(codeForPresence, false);
            }
            Hello hello = new Hello(userId);
            client.send(hello.createFormat());
            if (!knownContact) {
                Invitation invitation = new Invitation();
                invitation.setIdUser(userId);
                invitation.setUserName(userName);
                client.send(invitation.createFormat());
            }
        } catch (IOException e) {
            throw new OperationException(knownContact ? "No se logro enviar el saludo" : "No se logro enviar la invitacion");
        }
        IChatView view = this.view;
        if (view != null) {
            final String statusText = knownContact ? "Status: Enviando saludo..." : "Status: Enviando saludo e invitacion...";
            SwingUtilities.invokeLater(() -> view.updateStatus(statusText));
        }

    }

    public void onAcceptHelloReceived(AcceptHello acceptHello) {
        if (acceptHello == null || acceptHello.getIdUser() == null || acceptHello.getIdUser().isBlank()) {
            return;
        }
        helloPresence.put(acceptHello.getIdUser(), true);
        if (lastHelloIp != null && !lastHelloIp.isBlank()) {
            helloPresence.put(IP_KEY_PREFIX + lastHelloIp, true);
            lastHelloIp = null;
        }
        for (SocketClient client : clients.values()) {
            if (acceptHello.getIdUser().equals(client.getUID()) && client.getIp() != null && !client.getIp().isBlank()) {
                helloPresence.put(IP_KEY_PREFIX + client.getIp(), true);
            }
        }
        SocketClient sc = clients.get(acceptHello.getIdUser());
        updateContactIpIfNeeded(acceptHello.getIdUser(), sc);
        IChatView view = this.view;
        if (view != null) {
            SwingUtilities.invokeLater(view::refreshContactPresence);
        }
    }

    public void onDeclineHelloReceived(DeclineHello declineHello) {
        if (declineHello != null && declineHello.getIdUser() != null && !declineHello.getIdUser().isBlank()) {
            helloPresence.put(declineHello.getIdUser(), false);
            for (SocketClient client : clients.values()) {
                if (declineHello.getIdUser().equals(client.getUID()) && client.getIp() != null && !client.getIp().isBlank()) {
                    helloPresence.put(IP_KEY_PREFIX + client.getIp(), false);
                }
            }
        }
        if (lastHelloIp != null && !lastHelloIp.isBlank()) {
            helloPresence.put(IP_KEY_PREFIX + lastHelloIp, false);
            SocketClient target = null;
            for (SocketClient client : clients.values()) {
                if (lastHelloIp.equals(client.getIp())) {
                    target = client;
                    break;
                }
            }
            if (target != null) {
                clients.remove(target.getUID());
                target.close();
            }
            lastHelloIp = null;
        }
        IChatView view = this.view;
        if (view != null) {
            SwingUtilities.invokeLater(view::refreshContactPresence);
        }
    }
    public void onConfirmedReceived(ConfirmRecived confirmRecived) {
        if (confirmRecived == null || confirmRecived.getIdMessage() == null || confirmRecived.getIdMessage().isBlank()) {
            return;
        }
        readMessageIds.add(confirmRecived.getIdMessage());
        IChatView view = this.view;
        if (view != null) {
            String messageId = confirmRecived.getIdMessage();
            SwingUtilities.invokeLater(() -> view.markMessageRead(messageId));
        }
    }
    public void onDeleteMessageReceived(DeleteMessage deleteMessage) {}
    public void onPinMessageReceived(PinMessage pinMessage) {}
    public void onUniqueMessageReceived(UniqueMessage uniqueMessage) {}
    public void onThemeReceived(Theme theme) {}

//validar la ip
// Implemente la siguiente modificacion en nuestro proyecto. Respetando nuestro patron MVC modificando el proyecto para pasar  un contacto a un amigo. El comando debe ser 020|Id_usuario|Nombre|IP. No hagas con codigo ficil
public void shareContact(String recipientCode, String recipientIp, String contactId, String contactName, String contactIp) {
    if (contactId == null || contactId.isBlank()) {
        IChatView view = this.view;
        if (view != null) {
            SwingUtilities.invokeLater(() -> view.showMessage("El contacto a compartir no es válido."));
        }
        return;
    }
    SocketClient target = findClientByCodeOrIp(recipientCode, recipientIp);
    IChatView view = this.view;
    if (target == null) {
        if (view != null) {
            SwingUtilities.invokeLater(() -> view.showMessage("El contacto seleccionado no está conectado."));
        }
        return;
    }
    PasarContacto pasarContacto = new PasarContacto(contactId, contactName, contactIp);
    try {
        target.send(pasarContacto.createFormat());
        if (view != null) {
            SwingUtilities.invokeLater(() -> view.showMessage("Contacto compartido."));
        }
    } catch (IOException e) {
        if (view != null) {
            SwingUtilities.invokeLater(() -> view.showError("No se pudo compartir el contacto: " + e.getMessage()));
        }
    }
}
    private void handleContactShared(PasarContacto pasarContacto) {
        if (pasarContacto == null) {
            return;
        }
        String contactId = pasarContacto.getIdUser();
        if (contactId == null || contactId.isBlank()) {
            return;
        }
        if (localUserId != null && localUserId.equals(contactId)) {
            return;
        }
        saveSharedContact(contactId, pasarContacto.getUserName(), pasarContacto.getIp());
        IChatView view = this.view;
        if (view != null) {
            String name = pasarContacto.getUserName() != null ? pasarContacto.getUserName() : "Desconocido";
            SwingUtilities.invokeLater(() -> {
                view.showMessage("Contacto recibido: " + name);
                view.reloadContacts();
            });
        }
    }
    private void saveSharedContact(String userId, String userName, String ip) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String name = userName != null && !userName.isBlank() ? userName : "Desconocido";
        String incomingIp = ip != null ? ip : "";
        try {
            if (contactDao.existByCode(userId)) {
                AcceptHello.User.Contact existing = contactDao.findByCode(userId);
                String finalIp = (!incomingIp.isBlank())
                        ? incomingIp
                        : (existing != null ? existing.getIp() : "");
                String query = "UPDATE contact SET name='" + name + "', ip='" + finalIp + "' WHERE code='" + userId + "'";
                contactDao.update(query);
            } else {
                AcceptHello.User.Contact contact = AcceptHello.User.Contact.builder()
                        .code(userId)
                        .name(name)
                        .ip(incomingIp)
                        .build();
                contactDao.save(contact);
            }
        } catch (Exception e) {
            System.out.println("No se pudo guardar contacto compartido: " + e.getMessage());
        }
    }

}