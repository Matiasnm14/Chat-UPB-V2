package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
public class SecurityHello extends Command {
    private List<String> algorithms;

    @Override
    public String createFormat() {
        String list = algorithms == null || algorithms.isEmpty()
                ? ""
                : String.join(",", algorithms);
        return getID() + "|" + list + System.lineSeparator();
    }

    public SecurityHello() {
        super("014");
    }

    public SecurityHello(List<String> algorithms) {
        super("014");
        this.algorithms = algorithms;
    }

    public static SecurityHello parse(String command) {
        String[] parses = command.split(Pattern.quote("|"));
        if (parses.length != 2) {
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        List<String> algorithms = new ArrayList<>();
        String raw = parses[1].trim();
        if (!raw.isEmpty()) {
            String[] items = raw.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    algorithms.add(trimmed);
                }
            }
        }
        return new SecurityHello(algorithms);
    }
}
