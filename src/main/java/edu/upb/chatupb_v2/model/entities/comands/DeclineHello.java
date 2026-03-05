package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class DeclineHello extends Command {
    private String idUser;

    @Override
    public String createFormat() {
        if (idUser == null || idUser.isBlank()) {
            return getID() + System.lineSeparator();
        }
        return getID() + "|" + idUser + System.lineSeparator();
    }

    public DeclineHello() {
        super("006");
    }

    public DeclineHello(String idUser) {
        super("006");
        this.idUser = idUser;
    }

    public String getIdUser() {
        return idUser;
    }

    public static DeclineHello parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length == 1){
            return new DeclineHello();
        }
        if(parses.length == 2){
            return new DeclineHello(parses[1]);
        }
        if(parses.length != 1){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new DeclineHello();
    }
}
