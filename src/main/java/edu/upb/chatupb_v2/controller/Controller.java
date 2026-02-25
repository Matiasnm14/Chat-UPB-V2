package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.commands.*;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.view.JUi;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Controller {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    @Getter
    private Map<String, JUi> uis = new HashMap<>();
    private static Controller instance;
    public static Controller getInstance(){
        if (instance == null) instance = new Controller();
        return instance;
    }
    public  void addClients(SocketClient client){
        clients.putIfAbsent(client.getUID(),client);
    }
    public  void delClients(String idUser){clients.remove(idUser);}

    public void notificarUI(Command command) throws Exception {
        if (command instanceof Invitation){
            SocketClient.SocketListener sl = clients
                    .get(((Invitation) command)
                            .getIdUser())
                    .getListener()
                    .get(((Invitation) command)
                            .getIdUser());
            sl.onInvitationReceived((Invitation) command);
        }
        if (command instanceof Accept) {
            SocketClient.SocketListener sl = clients
                    .get(((Accept) command)
                            .getIdUser())
                    .getListener()
                    .get(((Accept) command)
                            .getIdUser());
            sl.onAcceptReceived((Accept) command);
        }
        if (command instanceof Decline){
            for (SocketClient sc : clients.values()){
                SocketClient.SocketListener sl = sc.getListener().get(sc.getUID());
                sl.onDeclineReceived((Decline) command);
            }
        }
        if (command instanceof Hello){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    sl.onHelloReceived((Hello) command);
                }
            }
        }
        if (command instanceof AcceptHello){
            SocketClient.SocketListener sl = clients
                    .get(((AcceptHello) command)
                            .getIdUser())
                    .getListener()
                    .get(((AcceptHello) command)
                            .getIdUser());
            sl.onAcceptHelloReceived((AcceptHello) command);
        }
        if (command instanceof DeclineHello){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    sl.onDeclineHelloReceived((DeclineHello) command);
                }
            }
        }

        if (command instanceof Chat) {
            SocketClient.SocketListener sl = clients
                    .get(((Chat) command)
                            .getIdUser())
                    .getListener()
                    .get(((Chat) command)
                            .getIdUser());
            sl.onChatReceived((Chat) command);
        }

        if (command instanceof ConfirmRecived){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    sl.onConfirmedReceived((ConfirmRecived) command);
                }
            }
        }

        if (command instanceof DeleteMessage){
            String id_message = ((DeleteMessage) command).getIdMessage();
            MessageDAO.getInstance().delete(id_message);
        }

        if (command instanceof Buzzing){
            SocketClient.SocketListener sl = clients.get(((Buzzing) command).getIdUser()).getListener().get(((Buzzing) command).getIdUser());
            sl.onBuzzingReceived((Buzzing) command);
        }


        if (command instanceof Bye){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
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
}
