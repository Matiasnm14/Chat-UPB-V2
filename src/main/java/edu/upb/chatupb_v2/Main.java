package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.model.network.ChatServer;
import edu.upb.chatupb_v2.controller.Mediator;
import edu.upb.chatupb_v2.controller.ProfileController;
import edu.upb.chatupb_v2.model.repository.ConnectionDB;
import edu.upb.chatupb_v2.view.JUi;
import edu.upb.chatupb_v2.view.LoginDialog;

public class Main {
    public static void main(String[] args) throws Exception {
        ConnectionDB.initDataBase();
        String userId = UserIdentity.loadOrCreateUserId().toString();
        ProfileController profileController = new ProfileController();
        String username = profileController.findUserName(userId);
        if (username == null || username.isBlank()) {
            LoginDialog.LoginResult login = LoginDialog.showDialog();
            if (login == null) {
                return;
            }
            username = login.getUsername();
            profileController.upsertUserName(userId, username);
        }
        JUi jUi = new JUi(username);
        Mediator mediator = Mediator.getInstance();
        mediator.setView(jUi);
        mediator.setLocalUser(userId, jUi.getUsername());
        new ChatServer();
        jUi.init();
    }
}