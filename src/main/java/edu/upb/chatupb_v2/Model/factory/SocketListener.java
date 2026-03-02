package edu.upb.chatupb_v2.Model.factory;

import edu.upb.chatupb_v2.Model.entities.comands.*;
import edu.upb.chatupb_v2.Model.network.SocketClient;

public interface SocketListener {
    void onInvitationReceived(Invitation invitation, SocketClient socketClient);
    void onAcceptReceived(Accept accept, SocketClient socketClient);
    void onDeclineReceived(Decline decline);
    void onHelloReceived(Hello hello);
    void onAcceptHelloReceived(AcceptHello acceptHello);
    void onDeclineHelloReceived(DeclineHello declineHello);
    void onChatReceived(Chat chat);
    void onConfirmedReceived(ConfirmRecived confirmRecived);
    void onDeleteMessageReceived(DeleteMessage deleteMessage);
    void onBuzzingReceived(Buzzing buzzing);
    void onPinMessageReceived(PinMessage pinMessage);
    void onUniqueMessageReceived(UniqueMessage uniqueMessage);
    void onThemeReceived(Theme theme);
    void onByeReceived(Bye bye);
}
