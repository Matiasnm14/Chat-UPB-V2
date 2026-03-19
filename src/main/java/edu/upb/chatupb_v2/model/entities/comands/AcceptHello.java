package edu.upb.chatupb_v2.model.entities.comands;

import edu.upb.chatupb_v2.model.entities.enums.StatusUser;
import edu.upb.chatupb_v2.model.Model;
import lombok.*;

import java.io.Serializable;
import java.util.regex.Pattern;

@Getter
@Setter
public class AcceptHello extends Command {
    private String idUser;
    @Override
    public String createFormat() {
        return getID() + "|" + getIdUser() + System.lineSeparator();
    }

    public AcceptHello() {
        super("005");
    }
    public AcceptHello(String idUser){
        super("005");
        this.idUser = idUser;
    }
    public static AcceptHello parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 2){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new AcceptHello(parses[1]);
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User implements Serializable, Model {

        private long id;
        private StatusUser statusUser;
        private String bio;

        public static final class Column{
            public static final String ID= "id";
            public static final String STATUSUSER ="status";
            public static final String BIO ="bio";
        }
        @Override
        public void setId(long id) {
            this.id = id;
        }

    @Override
    public long getId() {
            return id;
        }

    }
}
