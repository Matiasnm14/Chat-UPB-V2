package edu.upb.chatupb_v2.bl.server;

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
}
