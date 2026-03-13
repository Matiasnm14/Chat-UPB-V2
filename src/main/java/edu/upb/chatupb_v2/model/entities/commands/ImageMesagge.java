package edu.upb.chatupb_v2.model.entities.commands;

import edu.upb.chatupb_v2.controller.exception.OperationException;
import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.entities.enums.StatusMessage;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;
import edu.upb.chatupb_v2.model.network.SocketClient;
import edu.upb.chatupb_v2.model.repository.MessageDAO;
import lombok.Getter;

import java.time.LocalDate;
import java.util.regex.Pattern;
@Getter
public class ImageMesagge extends Command {

    private String idUser;
    private String idMessage;
    private String image;

    public ImageMesagge(String idUser, String idMessage, String image) {
        super("021");
        this.idMessage = idMessage;
        this.idUser = idUser;
        this.image = image;
    }


    @Override
    public String createFormat() {
        return getID() + "|" + idUser + "|" + idMessage + "|" + image + System.lineSeparator();

    }

    @Override
    public void executed(SocketClient sc) {

        Message msgDb = new Message(
                this.idMessage,
                sc.getUID(),
                this.image.toString(),
                TypeMessage.IMAGE,
                StatusMessage.SENT,
                LocalDate.now().toString()
        );


        try {
            MessageDAO.getInstance().save(msgDb);
//            view.onLoadMessages(MessageDAO.getInstance().findByContact(destinationId));

        } catch (Exception e) {
            throw new OperationException("Error en guardar el Mensaje en la base de datos");
        }


//        SocketClient sc = Controller.getInstance().getClients().get(destinationId);

        try{
            if (sc != null) {
                sc.send(createFormat());
//                SwingUtilities.invokeLater(() -> view.showMessage("Tú | " + messageText + " |Enviado"));
            } else {
//                SwingUtilities.invokeLater(() -> view.showError("El contacto no está en línea en este momento, pero el mensaje se guardó."));
            }
        }catch (Exception e){
            throw new OperationException("Error en enviar el chat a SocketClient");
        }
    }
    public static ImageMesagge parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 4){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new ImageMesagge(parses[1],parses[2],parses[3]);
    }
}
