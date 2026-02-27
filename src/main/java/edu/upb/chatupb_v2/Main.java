package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.model.network.ChatServer;
import edu.upb.chatupb_v2.view.JUi;

public class Main {
    public static void main(String[] args) {
        JUi jUi = new JUi();

        jUi.init();
        ContactController contactController = new ContactController(jUi);
        MessageController messageController = new MessageController(jUi);
        jUi.setContactController(contactController);
        contactController.onLoadContacts();
        jUi.setMessageController(messageController);
        Controller.getInstance().setServer(new ChatServer(jUi.getUsername(), jUi.getUserId()));
    }
}
