package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.model.repository.UserDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.List;

public class UserController {
    private ContactDao contactDao;
    private IChatView iChatView;
    public UserController(IChatView view){
        this.contactDao = ContactDao.getInstance();
        this.iChatView = view;
    }

    public List<User> getUsers() throws SQLException, ConnectException {
        return UserDao.getInstance().findAll();
    }
}
