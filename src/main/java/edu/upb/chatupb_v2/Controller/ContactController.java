package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Model.entities.User;
import edu.upb.chatupb_v2.Model.repository.UserDAO;
import edu.upb.chatupb_v2.VIews.IChatView;

public class ContactController {
    private UserDAO daoInstance = UserDAO.getInstance();
    private IChatView iChatView;

    public ContactController(IChatView iChatView){
        UserDAO us = UserDAO.getInstance();
        this.iChatView = iChatView;
    }

}
