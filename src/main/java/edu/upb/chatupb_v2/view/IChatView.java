package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;
import edu.upb.chatupb_v2.model.repository.MessageDAO;

import java.util.List;

public interface IChatView {
    void updateStatus(String status);
    void showMessage(String message);
    void showError(String error);
    boolean showInvitationDialog(String userName, String userId);
    void showBuzzNotification(String senderName);
    void showClientOffline(String senderName);
    void addChatMessage(String message, boolean outgoing, String senderName);
    void unload(List<AcceptHello.User.Contact> contacts);
    void unloadMessages(List<MessageDAO.Message> messages);
}
