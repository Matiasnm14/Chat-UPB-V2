package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class SecuritySelected extends Command {
    private String algorithm;
    private String key;

    @Override
    public String createFormat() {
        String algo = algorithm == null ? "" : algorithm;
        String keyValue = key == null ? "" : key;
        return getID() + "|" + algo + "|" + keyValue + System.lineSeparator();
    }

    public SecuritySelected() {
        super("015");
    }

    public SecuritySelected(String algorithm, String key) {
        super("015");
        this.algorithm = algorithm;
        this.key = key;
    }

    public static SecuritySelected parse(String command) {
        String[] parses = command.split(Pattern.quote("|"));
        if (parses.length != 3) {
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new SecuritySelected(parses[1], parses[2]);
    }
}
