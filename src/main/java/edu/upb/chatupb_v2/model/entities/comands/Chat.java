package edu.upb.chatupb_v2.model.entities.comands;

import edu.upb.chatupb_v2.controller.Controller;
import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.time.LocalDate;
import java.util.regex.Pattern;

@Getter
@Setter
public class Chat extends Command{
    private String idUser;
    private String idMessage;
    private String message;
    @Override
    public String createFormat() {
        return getID() + "|" + idUser + "|" + idMessage + "|" + message + System.lineSeparator();
    }

    @Override
    public void execute(SocketClient client) {
        try {


            Message msgDb = new Message(
                    idMessage,
                    client.getUID(),
                    message,
                    TypeMessage.TEXT,
                    StatusMessage.SENT,
                    LocalDate.now().toString()
            );
            MessageDAO.getInstance().save(msgDb);



            if (client != null) {
                client.send(createFormat());
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(idUser).
                        updateMessages());
            } else {
                SwingUtilities.invokeLater(() -> Controller.getInstance().getUis().get(idUser).
                        showError("El contacto no está en línea en este momento, pero el mensaje se guardó."));
            }

        } catch (Exception e) {
            throw new OperationException("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    public Chat() {
        super("007");
    }
    public Chat(String idUser,String idMessage,String message){
        super("007");
        this.idUser = idUser;
        this.idMessage = idMessage;
        this.message = message;
    }
    public static Chat parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 4){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Chat(parses[1],parses[2],parses[3]);
    }
}
