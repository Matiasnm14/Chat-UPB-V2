package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.MessageController;
import edu.upb.chatupb_v2.controller.UserController;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.repository.ConnectionDB;
import edu.upb.chatupb_v2.model.repository.UserDao;
import edu.upb.chatupb_v2.view.JUi;

import javax.swing.*;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws Exception {
        ConnectionDB.initDatabase();

        String miUsername;
        String miUserId;

        var users = UserDao.getInstance().findAll();

        if (users != null && !users.isEmpty()) {
            User user = users.getFirst();
            miUsername = user.getName();
            miUserId = user.getId();
            System.out.println("Auto-login exitoso: " + miUsername);
        } else {
            miUsername = JOptionPane.showInputDialog(null,
                    "Ingresa tu nombre de usuario:",
                    "Bienvenida a ChatUPB",
                    JOptionPane.QUESTION_MESSAGE);

            if (miUsername == null || miUsername.trim().isEmpty()) {
                System.exit(0);
            }

            miUserId = UUID.randomUUID().toString();
            UserDao.getInstance().save(new User(miUserId, miUsername));
        }

        JUi jUi = new JUi(miUsername, miUserId);

        ContactController contactController = new ContactController(jUi);
        MessageController messageController = new MessageController(jUi);
        UserController userController = new UserController(jUi);

        jUi.setContactController(contactController);
        jUi.setMessageController(messageController);
        jUi.setUserController(userController);

        jUi.init();
        contactController.onLoadContacts();
    }
}
