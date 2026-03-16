package edu.upb.chatupb_v2.Model.entities.comands;

import edu.upb.chatupb_v2.Model.network.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.regex.Pattern;

@Getter
@Setter
public class UniqueMessage extends Command{
    private String idMessage;
    private String sendUser;
    private String receiveUser;
    private String message;

    @Override
    public String createFormat() {
        return getID() + "|" + sendUser + "|" + idMessage + "|" + message + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient sc) {
        try {
            sc.send(createFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public UniqueMessage(String idUser,String idMessage, String message){
        super("012");
        this.sendUser = idUser;
        this.idMessage = idMessage;
        this.message = message;
    }
    public static UniqueMessage parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 4){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new UniqueMessage(parses[1],parses[2],parses[3]);
    }
}
