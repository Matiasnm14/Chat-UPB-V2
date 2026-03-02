package edu.upb.chatupb_v2.Controller.exceptions;

import edu.upb.chatupb_v2.Model.network.SocketClient;

public class ChatException extends RuntimeException {
    public ChatException(SocketClient sc) {
        super(sc.getIp() + " CONNECTION IS NOW CLOSED");
        sc.close();
    }
}
