package edu.upb.chatupb_v2.view;

import javax.swing.JFrame;

public abstract class IChatView extends JFrame {
    public abstract void updateStatus(String status);
    public abstract void showMessage(String message);
    public abstract void showError(String error);
    public abstract boolean showInvitationDialog(String userName, String userId);
    public abstract void showBuzzNotification(String senderName);
    public abstract void showClientOffline(String senderName);
    public abstract void addChatMessage(String message, boolean outgoing, String senderName);
}
