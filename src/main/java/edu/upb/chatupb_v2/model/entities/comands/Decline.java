package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter@Setter
public class Decline extends Command {
    private String idUser;

    @Override
    public String createFormat() {
        String safeUserId = idUser == null ? "" : idUser;
        return getID() + "|" + safeUserId + System.lineSeparator();
    }

    public Decline() {
        super("003");
    }

    public Decline(String idUser) {
        super("003");
        this.idUser = idUser;
    }

    public static Decline parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length == 2){
            return new Decline(parses[1]);
        }
        if(parses.length == 1){
            return new Decline("");
        }
        if(parses.length > 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Decline("");
    }
}
