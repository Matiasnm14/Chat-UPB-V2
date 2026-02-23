package edu.upb.chatupb_v2.repository.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class GoodBye extends Command{
    private String idUser;



    @Override
    public String createFormat() {
        return getID() + "|" + getIdUser() + System.lineSeparator();
    }

    public GoodBye() {
        super("0018");
    }
    public GoodBye(String idUser){
        super("0018");
        this.idUser = idUser;
    }
    public static GoodBye parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new GoodBye(parses[1]);
    }
}
