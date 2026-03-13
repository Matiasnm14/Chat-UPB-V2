package edu.upb.chatupb_v2.Model.entities.comands;

import edu.upb.chatupb_v2.Model.network.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Buzzing extends Command{
    private String idUser;



    @Override
    public String createFormat() {
        return getID() + "|" + idUser + "|" + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient sc) {

    }

    public Buzzing() {
        super("010");
    }
    public Buzzing(String idUser){
        super("010");
        this.idUser = idUser;
    }
    public static Buzzing parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Buzzing(parses[1]);
    }
}
