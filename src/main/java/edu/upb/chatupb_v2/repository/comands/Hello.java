package edu.upb.chatupb_v2.repository.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Hello extends Command{
    private String idUser;



    @Override
    public void createFormat() {

    }

    public Hello() {
        super("004");
    }
    private Hello(String idUser){
        super("004");
        this.idUser = idUser;
    }
    public static Command parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 3){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Hello(parses[1]);
    }
}
