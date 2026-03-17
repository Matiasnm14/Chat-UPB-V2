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

        @Builder
        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Contact implements Serializable, Model {
            public static final String ME_CODE = "af3bc20a-766c-4cd4-813d-b1067a01fa9a";


            public static final class Column{
                public static final String ID= "id";
                public static final String CODE ="code";
                public static final String NAME ="name";
                public static final String IP ="ip";
                public static final String THEME_ID = "theme_id";

            }
            @Override
            public void setId(long id) {
                this.id = id;
            }
            @Override
            public long getId() {
                return id;
            }
            private long id;
            private String code;
            private String name;
            private String ip;
            private String themeId;
            private boolean stateConnect = false;

            public String roomCode(){
                return ME_CODE + code;
            }


        }
    }
}
