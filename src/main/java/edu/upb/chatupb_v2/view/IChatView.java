package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;
import edu.upb.chatupb_v2.model.repository.MessageDAO;

import javax.swing.JFrame;
import java.util.List;

public abstract class IChatView extends JFrame {
    public abstract void updateStatus(String status);
    public abstract void showMessage(String message);
    public abstract void showError(String error);
    public abstract boolean showInvitationDialog(String userName, String userId);
    public abstract void showBuzzNotification(String senderName);
    public abstract void showClientOffline(String senderName);
    public abstract void addChatMessage(String message, boolean outgoing, String senderName);
    public abstract void unload(List<AcceptHello.User.Contact> contacts);
    public abstract void unloadMessages(List<MessageDAO.Message> messages);
}
