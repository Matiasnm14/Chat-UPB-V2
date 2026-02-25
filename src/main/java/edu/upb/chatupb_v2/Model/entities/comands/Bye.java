package edu.upb.chatupb_v2.Model.entities.comands;

import lombok.Getter;

import java.util.regex.Pattern;

public class Bye extends Command {
    @Getter
    private String idUser;
    private String userName;

    @Override
    public String createFormat() {
        return getID() +"|" +idUser + System.lineSeparator();
    }

    public Bye(){
        super("0018");
    }

    public Bye(String idUser){
        super("0018");
        this.idUser = idUser;
    }

    public static Bye parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Bye(parses[1]);
    }

}
