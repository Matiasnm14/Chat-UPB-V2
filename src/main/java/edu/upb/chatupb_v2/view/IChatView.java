package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;

import java.util.List;

public interface IChatView {
    void updateStatus(String status);
    void showMessage(String message);
    void showError(String error);
    boolean showInvitationDialog(String userName, String userId);
    void showBuzzNotification(String senderName);
    void showClientOffline(String senderName);
    void addChatMessage(String message, boolean outgoing, String senderName);
    void addChatMessage(String message, boolean outgoing, String senderName, String messageId);
    void addUniqueMessage(String message, boolean outgoing, String senderName, String messageId);
    void addImageMessage(String imageBase64, boolean outgoing, String senderName);
    void addImageMessage(String imageBase64, boolean outgoing, String senderName, String messageId);
    void unload(List<AcceptHello.User.Contact> contacts);
    void unloadMessages(List<ChatMessageViewModel> messages);
    void reloadContacts();
    void refreshContactPresence();
    void markMessageRead(String messageId);
    void removeMessage(String messageId);
    void showPinnedMessage(String messageId, String previewText);
    void applyTheme(String themeId);
    void applyThemeForContact(String contactCode, String themeId);
}
//
