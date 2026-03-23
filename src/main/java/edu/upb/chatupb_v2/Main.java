package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.Controller.ContactController;
import edu.upb.chatupb_v2.Model.network.ChatServer;
import edu.upb.chatupb_v2.VIews.JUi;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        JUi jUi = new JUi();
        ChatServer chatServer = new ChatServer(jUi.getUIController());
        ContactController conn = new ContactController(jUi);
        jUi.setController(conn);
        jUi.init();
    }
}
