package edu.upb.chatupb_v2.repository.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class ConfirmRecived extends Command{
    private String idMessage;

    @Override
    public String createFormat() {
        return getID()+"|"+getIdMessage()+System.lineSeparator();
    }

    public ConfirmRecived() {
        super("008");
    }
    public ConfirmRecived(String idMessage){
        super("008");
        this.idMessage = idMessage;
    }
    public static ConfirmRecived parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new ConfirmRecived(parses[1]);
    }
}
