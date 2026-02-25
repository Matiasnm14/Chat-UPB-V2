package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Model.entities.comands.Chat;

public interface IChatView {
    void updateStatus(String status);
    void showMessage(String message);
    void showError(String error);
    boolean showInvitationDialog(String userName, String userId);
    void showBuzzNotification(String senderName);
    void showChat(Chat chat);

    void renderContacts();
    void showByeNotification(String id);
}
