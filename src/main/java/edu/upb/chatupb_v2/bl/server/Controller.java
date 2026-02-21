package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.repository.comands.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    @Getter
    private Map<String, SocketClient> clients = new HashMap<>();
    private static Controller instance;
    public static Controller getInstance(){
        if (instance == null) instance = new Controller();
        return instance;
    }
    public  void addClients(SocketClient client){
        clients.putIfAbsent(client.getUID(),client);
    }
    public  void delClients(String idUser){clients.remove(idUser);}

    public void notificarUI(Command command){
        if (command instanceof Invitation){
            for (SocketClient sc : clients.values()){
                SocketClient.SocketListener sl = sc.getListener().get(sc.getUID());
                sl.onInvitationReceived((Invitation) command);
            }
        }
        if (command instanceof Accept) {
            for (SocketClient sc : clients.values()){
                SocketClient.SocketListener sl = sc.getListener().get(sc.getUID());
                sl.onAcceptReceived((Accept) command);
            }
        }
        if (command instanceof Decline){
            for (SocketClient sc : clients.values()){
                SocketClient.SocketListener sl = sc.getListener().get(sc.getUID());
                sl.onDeclineReceived((Decline) command);
            }
        }
        if (command instanceof Hello){
            for (SocketClient sc : clients.values()){
//                for (SocketClient.SocketListener sl : sc.getListener().values()){
//                    sl.onHelloReceived((Hello) command);
//                }
                SocketClient.SocketListener sl = sc.getListener().get(sc.getUID());
                sl.onHelloReceived((Hello) command);
            }
        }
        if (command instanceof AcceptHello){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    sl.onAcceptHelloReceived((AcceptHello) command);
                }
            }
        }
        if (command instanceof Bye){
            for (SocketClient sc : clients.values()){
                for (SocketClient.SocketListener sl : sc.getListener().values()){
                    sl.onByeReceived((Bye) command);
                }
            }
        }
    }
}
