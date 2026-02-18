package edu.upb.chatupb_v2.bl.server;

import lombok.Getter;

import java.util.HashMap;

public class Mediator {
    @Getter
    private static HashMap<String, SocketClient> clients = new HashMap();
    private static final Mediator MEDIATOR = new Mediator();

    private Mediator(){

    }
    public void addClients(SocketClient client){
        clients.put(client.getUID(), client);
    }

    public void removeClients(String id){
        clients.remove(id);
    }

    public static Mediator getInstance(){
        return MEDIATOR;
    }
}
