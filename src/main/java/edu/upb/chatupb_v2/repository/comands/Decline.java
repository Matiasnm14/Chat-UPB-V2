package edu.upb.chatupb_v2.repository.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter@Setter
public class Decline extends Command{

    @Override
    public String createFormat() {
        return null;
    }

    public Decline() {
        super("003");
    }

    public static Decline parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 1){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Decline();
    }
}
