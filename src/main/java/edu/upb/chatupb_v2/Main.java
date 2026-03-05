package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.controller.ChatServer;
import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.controller.Mediator;
import edu.upb.chatupb_v2.model.repository.UserProfileDao;
import edu.upb.chatupb_v2.view.JUi;
import edu.upb.chatupb_v2.view.LoginDialog;

public class Main {
    public static void main(String[] args) throws Exception {
        String userId = UserIdentity.loadOrCreateUserId().toString();
        UserProfileDao profileDao = new UserProfileDao();
        String username = profileDao.findUserName(userId);
        if (username == null || username.isBlank()) {
            LoginDialog.LoginResult login = LoginDialog.showDialog();
            if (login == null) {
                return;
            }
            username = login.getUsername();
            profileDao.upsertUserName(userId, username);
        }
        JUi jUi = new JUi(username);
        Mediator mediator = Mediator.getInstance();
        mediator.setView(jUi);
        mediator.setLocalUser(userId, jUi.getUsername());
        new ChatServer();
        ContactController contact = new ContactController(jUi);
        contact.unload();
        jUi.init();
    }
}
//HORA, NOMBRE,ICONO
//La diferencia entre strategy y fachada es lo mismo pero depennde en que lo usas
//