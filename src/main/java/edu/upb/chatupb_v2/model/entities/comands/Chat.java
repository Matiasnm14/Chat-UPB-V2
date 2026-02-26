package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Chat extends Command{
    private String idUser;
    private String idMessage;
    private String message;
    @Override
    public String createFormat() {
        return getID() + "|" + idUser + "|" + idMessage + "|" + message + System.lineSeparator();
    }

    public Chat() {
        super("007");
    }
    public Chat(String idUser,String idMessage,String message){
        super("007");
        this.idUser = idUser;
        this.idMessage = idMessage;
        this.message = message;
    }
    public static Chat parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 4){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Chat(parses[1],parses[2],parses[3]);
    }
}
