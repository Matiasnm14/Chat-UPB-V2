package edu.upb.chatupb_v2.model.entities.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import edu.upb.chatupb_v2.model.network.SocketClient;
@AllArgsConstructor
public abstract class Command {
    @Getter
    String ID;
    public abstract String createFormat();

    public abstract void executed(SocketClient sc);


}
