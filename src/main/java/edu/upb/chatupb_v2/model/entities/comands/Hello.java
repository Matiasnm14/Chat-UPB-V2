package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Hello extends Command{
    private String idUser;



    @Override
    public String createFormat() {
        return getID() + "|" + getIdUser() + System.lineSeparator();
    }

    public Hello() {
        super("004");
    }
    public Hello(String idUser){
        super("004");
        this.idUser = idUser;
    }
    public static Hello parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Hello(parses[1]);
    }
}
