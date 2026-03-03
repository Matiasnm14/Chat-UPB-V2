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
    private String sendUser;
    private String receiveUser;
    private String body;
    private TypeMessage typeMessage;
    private StatusMessage statusMessage;
    private String date;
    public static final class Column{
        public static final String ID_MESSAGE = "id_message";
        public static final String SENDER_ID = "sender_id";
        public static final String RECEIVER_ID = "receiver_id";
        public static final String BODY ="body";
        public static final String TYPEMESSAGE ="type";
        public static final String STATUSMESSAGE = "Status";
        public static final String DATE = "date";
    }

    public void setId(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getId() {
        return " ";
    }

}
