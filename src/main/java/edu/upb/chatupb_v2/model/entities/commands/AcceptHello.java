package edu.upb.chatupb_v2.model.entities.commands;

import edu.upb.chatupb_v2.model.network.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class AcceptHello extends Command{
    private String idUser;
    @Override
    public String createFormat() {
        return getID() + "|" + getIdUser() + System.lineSeparator();
    }

    @Override
    public void executed(SocketClient sc) {

    }


    public AcceptHello() {
        super("005");
    }
    public AcceptHello(String idUser){
        super("005");
        this.idUser = idUser;
    }
    public static AcceptHello parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new AcceptHello(parses[1]);
    }
}
