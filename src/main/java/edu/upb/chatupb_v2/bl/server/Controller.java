package edu.upb.chatupb_v2.bl.server;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    @Getter
    static Map<String, SocketClient> clients = new HashMap<>();

    public static void addClients(SocketClient client){
        clients.putIfAbsent(client.getUID(),client);
    }

    public static void delClients(String idUser){clients.remove(idUser);}
}
