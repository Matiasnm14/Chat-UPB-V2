package edu.upb.chatupb_v2.bl.server;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    @Getter
    static List<SocketClient> clients = new ArrayList<>();

    public static void addClients(SocketClient client){
        clients.add(client);
    }
}
