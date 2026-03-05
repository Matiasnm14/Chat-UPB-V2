package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.repository.UserDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.List;

public class UserController {
    private UserDao userDao;

    private IChatView view;

    public UserController (IChatView view){
        userDao = UserDao.getInstance();
        this.view = view;
    }

    public String onLoadUser(){
        try {
            List<User> users = userDao.findAll();
            return view.onLoadUser(users);
        }catch (Exception E){
            throw new OperationException("Error al recuperar el usuario");
        }
    }
}
