package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class UniqueMessage extends Command{
    private String idUser;
    private String idMessage;
    private String message;


    @Override
    public String createFormat() {
        return null;
    }

    public UniqueMessage() {
        super("012");
    }
    public UniqueMessage(String idUser,String idMessage, String message){
        super("012");
        this.idMessage = idMessage;
        this.idUser = idUser;
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
