package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.network.ChatServer;
import edu.upb.chatupb_v2.view.JUi;
import edu.upb.chatupb_v2.model.entities.comands.*;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
//import edu.upb.chatupb_v2.model.repository.comands.*;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller implements SocketClient.SocketListener {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    @Getter
    private Map<String, JUi> uis = new HashMap<>();
    private static Controller instance;
    @Getter
    private List<SocketClient> pendingClients = new ArrayList<>();

    private ChatServer server;

    public static Controller getInstance() {
        if (instance == null) instance = new Controller();
        return instance;
    }

    public void addClients(SocketClient client) {
        clients.putIfAbsent(client.getUID(), client);
    }

    public void delClients(String idUser) {
        clients.remove(idUser);
    }

    @Override
    public void notificarUI(Command command, String clientId) throws Exception {
        for (JUi sl : uis.values()) {
            if (command instanceof Invitation) {
//                SocketClient.SocketListener sl = clients.get(((Invitation) command).getIdUser()).getListener().get(((Invitation) command).getIdUser());
                sl.onInvitationReceived((Invitation) command);
            }
            if (command instanceof Accept) {

                sl.onAcceptReceived((Accept) command);
            }
            if (command instanceof Decline) {
                for (SocketClient sc : clients.values()) {

                    sl.onDeclineReceived((Decline) command);
                    SocketClient socketClient = clients.get(clientId);
                    delClients(socketClient.getUID());
                    if (socketClient != null) socketClient.close();
                }
            }
            if (command instanceof Hello) {
                for (SocketClient sc : clients.values()) {

                    sl.onHelloReceived((Hello) command);

                }
            }
            if (command instanceof AcceptHello) {

                sl.onAcceptHelloReceived((AcceptHello) command);
            }
            if (command instanceof DeclineHello) {
                for (SocketClient sc : clients.values()) {

                    sl.onDeclineHelloReceived((DeclineHello) command);

                }
            }

            if (command instanceof Chat) {

                sl.onChatReceived((Chat) command);

            }

            if (command instanceof ConfirmRecived) {
                for (SocketClient sc : clients.values()) {

                    sl.onConfirmedReceived((ConfirmRecived) command);

                }
            }

            if (command instanceof DeleteMessage) {
                String id_message = ((DeleteMessage) command).getIdMessage();
                MessageDAO.getInstance().delete(id_message);
            }

            if (command instanceof Buzzing) {

                sl.onBuzzingReceived((Buzzing) command);
            }


            if (command instanceof Bye) {
                for (SocketClient sc : clients.values()) {

                    sl.onByeReceived((Bye) command);

                }
            }
        }

    }

    public void addUi(JUi ui) {
        uis.putIfAbsent(ui.getUserId().toString(), ui);
    }
//    public void addContact(String contact){
//        for (JUi view : uis.values()){
//            view.addModel(contact);
//        }
//    }

    public void delUi(String idUi) {
        uis.remove(idUi);
    }

    public ChatServer createServer(JUi view){
        if (server == null){
            server = new ChatServer(view.getUsername(), view.getUserId());
        }
        return server;
    }

    public void sendMessage(String texto, String idContacto){
        if (clients.containsKey(idContacto)){
            server.sendMessage(texto, idContacto);
        }
    }

    public void sendBuzz(){
        server.sendBuzz();
    }

    public void sendBye(){
        server.sendBye();
    }

    public void connect(String ip){
        server.connect(ip);
    }
}
