package edu.upb.chatupb_v2.Model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class DeclineHello extends Command{

    @Override
    public String createFormat() {
        return null;
    }

    public DeclineHello() {
        super("006");
    }

    public static DeclineHello parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 1){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new DeclineHello();
    }
}
