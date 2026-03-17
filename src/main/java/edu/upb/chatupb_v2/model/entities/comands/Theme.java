package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@Getter
@Setter
public class Theme extends Command {
    private String idUser;
    private String idTheme;

    @Override
    public String createFormat() {
        String safeUserId = idUser == null ? "" : idUser;
        String safeThemeId = idTheme == null ? "" : idTheme;
        return getID() + "|" + safeUserId + "|" + safeThemeId + System.lineSeparator();
    }

    public Theme() {
        super("013");
    }
    public Theme(String idUser,String idTheme){
        super("013");
        this.idUser = idUser;
        this.idTheme = idTheme;
    }
    public static Theme parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 3){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new Theme(parses[1],parses[2]);
    }
}
