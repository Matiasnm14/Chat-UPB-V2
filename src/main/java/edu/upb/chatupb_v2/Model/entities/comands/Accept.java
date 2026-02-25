package edu.upb.chatupb_v2.Model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter@Setter
public class Accept extends Command{
    private String idUser;
    private String userName;



    @Override
    public String createFormat() {
        return getID() + "|" + this.idUser + "|" + this.userName +System.lineSeparator();
    }

    public static Accept parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 3){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Accept(parses[1],parses[2]);
    }

    public Accept(String idUser,String userName){
        super("002");
        this.idUser = idUser;
        this.userName = userName;
    }
}
