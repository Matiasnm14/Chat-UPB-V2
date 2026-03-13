package edu.upb.chatupb_v2.Model.entities.comands;

import edu.upb.chatupb_v2.Model.network.SocketClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
public abstract class Command {
    @Getter
    String ID;
    public abstract String createFormat();

    public abstract void execute(SocketClient sc);
}
