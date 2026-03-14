package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Model.entities.User;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.factory.SocketListener;
import edu.upb.chatupb_v2.Model.network.SocketClient;
import edu.upb.chatupb_v2.Model.repository.MessageDAO;
import edu.upb.chatupb_v2.Model.repository.UserDAO;
import lombok.Getter;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ClientController {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    private static ClientController instance;
    public static synchronized ClientController getInstance(){
        if (instance == null) instance = new ClientController();
        return instance;
    }
    public  void delClients(String idUser){clients.remove(idUser);}
    public static String my_uid;
    private final Map<String, Queue<String>> pendingMessages = new HashMap<>();
    private final Map<SocketClient, String> socketToTargetId = new HashMap<>();

    public void sendToClient(String targetId, String targetIp, Chat chat, SocketListener listener) throws IOException {
        SocketClient sc = clients.get(targetId);
        if (sc == null) {
            pendingMessages.computeIfAbsent(targetId, k -> new LinkedList<>()).add(chat.createFormat());
            SocketClient newSc = new SocketClient(targetIp);
            newSc.setSocketListener(listener);
            scw = newSc;
            newSc.start();
            socketToTargetId.put(newSc, targetId); // recordar a quién va dirigido
            try {
                new Hello(((UIController) listener).getUserId()).execute(newSc);
            } catch (Exception e){
                System.out.println(e);
            }
        } else {
            chat.execute(sc);
        }
    }

    public void sendToClient(String targetId, String targetIp, Command command, SocketListener listener) throws IOException {
        SocketClient sc = clients.get(targetId);
        if (sc == null) {
            pendingMessages.computeIfAbsent(targetId, k -> new LinkedList<>()).add(command instanceof Chat ? ((Chat)command).createFormat() : ((Image)command).createFormat());
            SocketClient newSc = new SocketClient(targetIp);
            newSc.setSocketListener(listener);
            scw = newSc;
            newSc.start();
            socketToTargetId.put(newSc, targetId); // recordar a quién va dirigido
            try {
                new Hello(((UIController) listener).getUserId()).execute(newSc);
            } catch (Exception e){
                System.out.println(e);
            }
        } else {
            if (command instanceof Chat) {
                ((Chat) command).execute(sc);
            } else if (command instanceof Image) {
                ((Image) command).execute(sc);
            }
        }
    }

    public void sendToClientUnique(String targetId, String targetIp, Command command, SocketListener listener) throws IOException {
        SocketClient sc = clients.get(targetId);
        if (sc == null) {
            pendingMessages.computeIfAbsent(targetId, k -> new LinkedList<>()).add(((UniqueMessage)command).createFormat());
            SocketClient newSc = new SocketClient(targetIp);
            newSc.setSocketListener(listener);
            scw = newSc;
            newSc.start();
            socketToTargetId.put(newSc, targetId); // recordar a quién va dirigido
            try {
                new Hello(((UIController) listener).getUserId()).execute(newSc);
            } catch (Exception e){
                System.out.println(e);
            }
        } else {
            ((UniqueMessage)command).execute(sc);
        }
    }

    public void senda(User user) throws IOException {
        System.out.println(user);
        NewFriend newFriend = new NewFriend(user.getId(), user.getName(), user.getIp());
        scw.send(newFriend.createFormat());
    }

    public void flushPending(SocketClient sc) throws IOException {
        String targetId = socketToTargetId.remove(sc);
        if (targetId == null) return;
        Queue<String> pending = pendingMessages.remove(targetId);
        if (pending == null) return;
        while (!pending.isEmpty()) {
            sc.send(pending.poll());
        }
    }

    public void registerClient(SocketClient client) {

        String uid = client.getUID();

        if (uid == null) {
            throw new IllegalStateException("Cannot register client without UID");
        }

        clients.compute(uid, (key, existingClient) -> {
            if (existingClient != null && existingClient == client) {
                System.out.println("Cliente ya registrado (mismo socket): " + uid);
                return existingClient;
            }

            if (existingClient == null) {
                System.out.println("Nuevo cliente registrado: " + uid);
                if (!userInDB(uid)) {
                    try {
                        UserDAO.getInstance().save(new User(client.getUID(), client.getNombre(), client.getIp(), my_uid));
                        System.out.println("REGISTRADO EN BD: " + uid);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                System.out.println("Cliente reconectado con nuevo socket: " + uid);
            }

            return client;
        });
    }

    public boolean userInDB(String id){
        User user = null;
        try {
            user = UserDAO.getInstance().findById(id);
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (user != null){
            return true;
        }
        return false;
    }

    public void connectTo(String ip, String userId, String username, SocketListener listener) throws IOException {
        if (my_uid == null){
            my_uid = userId;
        }
        SocketClient sc = new SocketClient(ip);
        sc.setSocketListener(listener);
        sc.start();
        Invitation inv = new Invitation(userId, username);
        sc.send(inv.createFormat());
    }

    SocketClient scw;
    public void connectToPrevious(String ip, String userId, SocketListener listener) throws IOException {
        if (my_uid == null){
            my_uid = userId;
        }
        SocketClient sc = new SocketClient(ip);
        scw = sc;
        sc.setSocketListener(listener);
        sc.start();
        Hello hel = new Hello(userId);
        sc.send(hel.createFormat());
    }

}