package edu.upb.chatupb_v2.bl.server;

import edu.upb.chatupb_v2.repository.Contact;
import edu.upb.chatupb_v2.repository.comands.Chat;

public interface IChatView {
    void updateStatus(String status);
    void showMessage(String message);
    void showError(String error);
    boolean showInvitationDialog(String userName, String userId);
    void showBuzzNotification(String senderName);
    void showChat(Chat chat);
    void showByeNotification(String id);
    void addModel(Contact contact);
}
