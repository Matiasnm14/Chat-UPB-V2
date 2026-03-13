package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class ImageMessage extends Command {
    private String idUser;
    private String idMessage;
    private String imageBase64;

    @Override
    public String createFormat() {
        String safeUser = idUser == null ? "" : idUser;
        String safeMessage = idMessage == null ? "" : idMessage;
        String safeImage = imageBase64 == null ? "" : imageBase64;
        return getID() + "|" + safeUser + "|" + safeMessage + "|" + safeImage + System.lineSeparator();
    }

    public ImageMessage() {
        super("021");
    }

    public ImageMessage(String idUser, String idMessage, String imageBase64) {
        super("021");
        this.idUser = idUser;
        this.idMessage = idMessage;
        this.imageBase64 = imageBase64;
    }

    public static ImageMessage parse(String command) {
        String[] parses = command.split(Pattern.quote("|"));
        if (parses.length != 4) {
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new ImageMessage(parses[1], parses[2], parses[3]);
    }
}
