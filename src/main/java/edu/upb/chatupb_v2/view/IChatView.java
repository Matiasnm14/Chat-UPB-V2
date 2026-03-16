package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.commands.Chat;

import java.awt.*;
import java.util.List;

public interface IChatView {
    void updateStatus(String status);
    void showMessage(String message);
    void showError(String error);
    boolean showInvitationDialog(String userName, String userId);
    void showBuzzNotification(String senderName);
    void showChat(Chat chat);
    void showByeNotification(String id);
    void onLoadContacts(List<Contact> contacts);
    void onLoadMessages(List<Message> messages);
    void onAddModel(Contact contact);
    void refreshChatView();
    Contact getCurrentContact();
    void showImageMessage(edu.upb.chatupb_v2.model.entities.commands.ImageMesagge imageMessage);
    void applyTheme(String themeId);
    void appendMessageToChat(Message msg);
    void updatePinnedMessageUI(String text);
}
