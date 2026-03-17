package edu.upb.chatupb_v2.VIews;

import edu.upb.chatupb_v2.Model.entities.Message;
import edu.upb.chatupb_v2.Model.entities.comands.Chat;
import edu.upb.chatupb_v2.Model.entities.comands.Image;
import edu.upb.chatupb_v2.Model.entities.comands.Theme;
import edu.upb.chatupb_v2.Model.entities.comands.UniqueMessage;

import java.util.List;

public interface IChatView {
    void updateStatus(String status);
    void showMessage(String message);
    void showError(String error);
    boolean showInvitationDialog(String userName, String userId);
    void showBuzzNotification(String senderName);
    void showChat(Chat chat);
    void showUniqueMessage(UniqueMessage uni);
    void renderContacts();
    void renderMessages(List<Message> list);
    void showByeNotification(String id);
    void onNewConnectionEstablished(String userName, String userId); // agrega userId
    void changeThemeSelected(Theme theme);
    void showImage(Image image);
    void showPinnedMessage(String messageId);

    void addMessage(String text, boolean isOwn, String idMessage);
}
