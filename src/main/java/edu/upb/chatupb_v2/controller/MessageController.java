package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.view.IChatView;

public class MessageController {
    MessageDAO messageDAO;
    IChatView iChatView;

    public MessageController(IChatView view){
        this.messageDAO = MessageDAO.getInstance();
        iChatView = view;
    }
    public void onLoadMessages(String contactId){
        try {
            java.util.List<Message> messages = MessageDAO.getInstance().findByContact(contactId);
            iChatView.onLoadMessages(messages);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
