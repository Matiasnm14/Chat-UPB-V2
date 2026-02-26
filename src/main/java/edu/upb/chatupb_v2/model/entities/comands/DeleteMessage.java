package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class DeleteMessage extends Command {
    private String idMessage;

    @Override
    public String createFormat() {
        return null;
    }

    public DeleteMessage() {
        super("009");
    }
    public DeleteMessage(String idMessage){
        super("009");
        this.idMessage = idMessage;
    }
    public static DeleteMessage parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new DeleteMessage(parses[1]);
    }
}
