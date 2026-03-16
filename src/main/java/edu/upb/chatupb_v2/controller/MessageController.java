package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
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

    public void updateReceived(String idMessage){
        try {

            messageDAO.updateMessageReceived(idMessage);
        }catch (Exception e){
            throw new OperationException("Error al actualizar el mensaje");
        }
    }

    public String obtainContact(String id) throws Exception{
        return messageDAO.findById(id).getContactId();
    }

    public void delete(String id_message){
        try {
            messageDAO.delete(id_message);
        }catch (Exception e){
            throw new OperationException("Error al borrar el mensaje");
        }
    }
}
