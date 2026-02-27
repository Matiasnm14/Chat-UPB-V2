package edu.upb.chatupb_v2.Model.entities;

import edu.upb.chatupb_v2.Model.entities.enums.StatusMessage;
import edu.upb.chatupb_v2.Model.entities.enums.TypeMessage;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable, Model {

    private String idMessage;
    private String idUser;
    private String body;
//    private TypeMessage typeMessage;
//    private StatusMessage statusMessage;
    private String date;
    public static final class Column{
        public static final String ID_MESSAGE = "id_message";
        public static final String ID_USER = "id_user";
        public static final String BODY ="body";
//        public static final String TYPEMESSAGE ="type";
//        public static final String STATUSMESSAGE = "Status";
//        public static final String STATUSMESSAGE = "Status";
        public static final String DATE = "date";
    }

    public void setId(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getId() {
        return " ";
    }

    @Override
    public String toString() {
        return "Message{" +
                "idMessage='" + idMessage + '\'' +
                ", idUser='" + idUser + '\'' +
                ", body='" + body + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
