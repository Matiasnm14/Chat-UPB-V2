package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.User;
import edu.upb.chatupb_v2.model.entities.comands.Chat;

import java.util.List;

public interface IChatView {
    void updateStatus(String status);
    void showMessage(String message);
    void showError(String error);
    boolean showInvitationDialog(String userName, String userId);
    void showBuzzNotification(String senderName);
    void showChat(Chat chat);
    void showByeNotification(String id);
    void addModel(Contact contact);
    void onLoadContacts(List<Contact> contacts);
    void onLoadMessages(List<Message> messages);
    String onLoadUser(List<User> users);
}
