package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class PasarContacto extends Command {
    private String idUser;
    private String userName;
    private String ip;

    @Override
    public String createFormat() {
        String safeId = idUser == null ? "" : idUser;
        String safeName = userName == null ? "" : userName;
        String safeIp = ip == null ? "" : ip;
        return getID() + "|" + safeId + "|" + safeName + "|" + safeIp + System.lineSeparator();
    }

    public PasarContacto(String idUser, String userName, String ip) {
        super("020");
        this.idUser = idUser;
        this.userName = userName;
        this.ip = ip;
    }

    public static PasarContacto parse(String command) {
        String[] parses = command.split(Pattern.quote("|"));
        if (parses.length != 4) {
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new PasarContacto(parses[1], parses[2], parses[3]);
    }
}
