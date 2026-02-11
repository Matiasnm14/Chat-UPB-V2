package edu.upb.chatupb_v2.repository.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class AcceptHello extends Command{
    private String idUser;
    @Override
    public void createFormat() {

    }

    public AcceptHello() {
        super("005");
    }
    public AcceptHello(String idUser){
        super("005");
        this.idUser = idUser;
    }
    public static Command parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new AcceptHello(parses[1]);
    }
}
