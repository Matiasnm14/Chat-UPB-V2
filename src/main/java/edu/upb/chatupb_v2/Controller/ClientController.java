package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Model.entities.User;
import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.factory.SocketListener;
import edu.upb.chatupb_v2.Model.network.SocketClient;
import edu.upb.chatupb_v2.Model.repository.MessageDAO;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClientController {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    private static ClientController instance;
    public static ClientController getInstance(){
        if (instance == null) instance = new ClientController();
        return instance;
    }
    public  void delClients(String idUser){clients.remove(idUser);}

    public void registerClient(SocketClient client) {

        String uid = client.getUID();

        if (uid == null) {
            throw new IllegalStateException("Cannot register client without UID");
        }

        clients.compute(uid, (key, existingClient) -> {
            if (existingClient == null) {
                System.out.println("Nuevo cliente registrado: " + uid);
                return client;
            }

            System.out.println("Cliente reconectado: " + uid);

            existingClient.close();
            return client;
        });
    }

    public void notificarUI(Command command) throws Exception {
        if (command instanceof Invitation){
            SocketListener sl = clients.get(((Invitation) command).getIdUser()).getSocketListener();
            sl.onInvitationReceived((Invitation) command);
        }
        if (command instanceof Accept) {
            SocketListener sl = clients.get(((Accept) command).getIdUser()).getSocketListener();
            sl.onAcceptReceived((Accept) command);
        }
//        if (command instanceof Decline){
//            for (SocketClient sc : clients.values()){
//                SocketListener sl = sc.get(sc.getUID());
//                sl.onDeclineReceived((Decline) command);
//            }
//        }
//        if (command instanceof Hello){
//            for (SocketClient sc : clients.values()){
//                for (SocketListener sl : sc.getListener().values()){
//                    sl.onHelloReceived((Hello) command);
//                }
//            }
//        }
//        if (command instanceof AcceptHello){
//            SocketListener sl = clients
//                    .get(((AcceptHello) command)
//                            .getIdUser())
//                    .getListener()
//                    .get(((AcceptHello) command)
//                            .getIdUser());
//            sl.onAcceptHelloReceived((AcceptHello) command);
//        }
//        if (command instanceof DeclineHello){
//            for (SocketClient sc : clients.values()){
//                for (SocketListener sl : sc.getListener().values()){
//                    sl.onDeclineHelloReceived((DeclineHello) command);
//                }
//            }
//        }
//
//        if (command instanceof Chat) {
//            SocketListener sl = clients
//                    .get(((Chat) command)
//                            .getIdUser())
//                    .getListener()
//                    .get(((Chat) command)
//                            .getIdUser());
//            sl.onChatReceived((Chat) command);
//        }
//
//        if (command instanceof ConfirmRecived){
//            for (SocketClient sc : clients.values()){
//                for (SocketListener sl : sc.getListener().values()){
//                    sl.onConfirmedReceived((ConfirmRecived) command);
//                }
//            }
//        }
//
//        if (command instanceof DeleteMessage){
//            String id_message = ((DeleteMessage) command).getIdMessage();
//            MessageDAO.getInstance().delete(id_message);
//        }
//
//        if (command instanceof Buzzing){
//            SocketListener sl = clients.get(((Buzzing) command).getIdUser()).getListener().get(((Buzzing) command).getIdUser());
//            sl.onBuzzingReceived((Buzzing) command);
//        }
//
//
//        if (command instanceof Bye){
//            for (SocketClient sc : clients.values()){
//                for (SocketListener sl : sc.getListener().values()){
//                    sl.onByeReceived((Bye) command);
//                }
//            }
//        }
    }

    public void selectedUserAction(User user){
        SocketClient cs = null;
        try {
            cs = new SocketClient(user.getIp());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Invitation inv = new Invitation();
        try {
            cs.send(inv.createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
