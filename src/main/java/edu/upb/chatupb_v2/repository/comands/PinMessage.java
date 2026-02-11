package edu.upb.chatupb_v2.repository.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class PinMessage extends Command{
    private String idMessage;

    @Override
    public void createFormat() {

    }

    public PinMessage() {
        super("011");
    }
    public PinMessage(String idMessage){
        super("011");
        this.idMessage = idMessage;
    }
    public static Command parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new PinMessage(parses[1]);
    }
}
