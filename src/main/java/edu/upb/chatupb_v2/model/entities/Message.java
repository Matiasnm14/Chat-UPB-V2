package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable, Model {

    private String idMessage; // PK
    private String contactId; // FK hacia Contacts
    private String body;
    private TypeMessage typeMessage;
    private StatusMessage statusMessage;
    private String date;

    public static final class Column {
        public static final String ID_MESSAGE = "id"; // Según el query inicial
        public static final String CONTACT_ID = "Contacts_id"; // El enlace real
        public static final String BODY = "message";
        public static final String TYPE = "type";
        public static final String STATUS = "status";
        public static final String DATE = "date";
    }

    public void setId(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getId() {
        return " ";
    }
}
