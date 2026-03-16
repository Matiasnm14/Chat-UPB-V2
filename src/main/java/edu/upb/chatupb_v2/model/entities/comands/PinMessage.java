package edu.upb.chatupb_v2.model.entities.comands;

import edu.upb.chatupb_v2.model.network.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;

@Getter
@Setter
public class PinMessage extends Command{
    private String idMessage;

    @Override
    public String createFormat() {
        return getID() + "|" + idMessage + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        try {
            client.send(createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PinMessage() {
        super("011");
    }
    public PinMessage(String idMessage){
        super("011");
        this.idMessage = idMessage;
    }
    public static PinMessage parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new PinMessage(parses[1]);
    }
}
