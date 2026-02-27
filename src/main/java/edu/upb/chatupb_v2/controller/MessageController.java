package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.List;

public class MessageController {

    private MessageDAO messageDAO;

    private IChatView view;

    public MessageController(IChatView view){
        messageDAO = MessageDAO.getInstance();
        this.view = view;
    }

    public void onLoadMessages(String contactId){
        try {
            List<Message> messages = messageDAO.findByContact(contactId);
            view.onLoadMessages(messages);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void save(Message mesg) throws Exception{
        messageDAO.save(mesg);
    }
}
