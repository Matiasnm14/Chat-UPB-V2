package edu.upb.chatupb_v2.repository.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Invitation extends Command{
    private String idUser;
    private String userName;

    @Override
    public String createFormat() {
        return getID() +"|" +idUser +"|" +  userName + System.lineSeparator();
    }

    public Invitation(){
        super("001");
    }

    public Invitation(String idUser,String userName){
        super("001");
        this.idUser = idUser;
        this.userName = userName;
    }
    public static Invitation parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 3){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Invitation(parses[1],parses[2]);
    }

}
