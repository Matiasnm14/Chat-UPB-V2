package edu.upb.chatupb_v2.model.entities.comands;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.network.SocketClient;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class DeleteMessage extends Command{
    private String idMessage;

    @Override
    public String createFormat() {
        return getID() + "|" + idMessage + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        try {
            if (client != null) client.send(createFormat());
        }catch (Exception e){
            throw new OperationException("Error al enviar DeleteMessage");
        }

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
