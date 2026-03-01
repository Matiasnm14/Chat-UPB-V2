package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Controller.exceptions.ConnectionException;
import edu.upb.chatupb_v2.Controller.exceptions.DatabaseException;
import edu.upb.chatupb_v2.Model.entities.Message;
import edu.upb.chatupb_v2.Model.entities.User;
import edu.upb.chatupb_v2.Model.repository.MessageDAO;
import edu.upb.chatupb_v2.Model.repository.UserDAO;
import edu.upb.chatupb_v2.VIews.IChatView;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.List;

public class ContactController {
    private UserDAO daoInstance = UserDAO.getInstance();
    private IChatView iChatView;

    public ContactController(IChatView iChatView){
        UserDAO us = UserDAO.getInstance();
        this.iChatView = iChatView;
    }
    public List<User> returnContacts() throws SQLException, ConnectException {
        return daoInstance.findAll();
    }
    public List<Message> returnMessages(String id_me, String id_other) {
        List<Message> messages = null;
        try {
             messages = MessageDAO.getInstance().getConversation(id_me, id_other);
        } catch (SQLException sql){
            throw new DatabaseException("Conexión a Base de Datos Fallida");
        } catch (ConnectException conn){
            throw new ConnectionException("Problemas en la Conexión");
        }
        return messages;
    }

}
