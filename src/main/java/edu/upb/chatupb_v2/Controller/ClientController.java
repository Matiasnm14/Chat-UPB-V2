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
    public static synchronized ClientController getInstance(){
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
            // Si es el mismo objeto, no hay nada que hacer
            if (existingClient == client) {
                System.out.println("Cliente ya registrado (mismo socket): " + uid);
                return existingClient;
            }
            // Solo cerrar si es un socket diferente (reconexión real)
            System.out.println("Cliente reconectado con nuevo socket: " + uid);
            //existingClient.close();
            return client;
        });
    }

    public void connectTo(String ip, String userId, String username, SocketListener listener) throws IOException {
        SocketClient sc = new SocketClient(ip);
        sc.setSocketListener(listener);
        sc.start();
        Invitation inv = new Invitation(userId, username);
        sc.send(inv.createFormat());
        // No registrar aquí — el registro ocurre cuando llega el Accept (handleAccept)
    }

////    public void selectedUserAction(User user){
////        SocketClient cs = null;
////        try {
////            cs = new SocketClient(user.getIp());
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
////        Invitation inv = new Invitation("MY-USER", "ME");
////        try {
////            cs.send(inv.createFormat());
////        } catch (IOException e) {
////            throw new RuntimeException(e);
////        }
////    }

}
