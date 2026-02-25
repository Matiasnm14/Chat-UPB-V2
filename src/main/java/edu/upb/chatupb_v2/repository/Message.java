package edu.upb.chatupb_v2.repository;

import edu.upb.chatupb_v2.repository.enums.TypeMessage;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable, Model {

    private long id;
    private String codMessage;
    private String recipientCode;
    private String senderCode;
    private String message;
    private TypeMessage type;
    private String createdDate;
    private String roomCode;
    public static final class Column{
        public static final String ID= "id";
        public static final String COD_MESSAGE ="cod_message";
        public static final String RECIPIENT_CODE ="recipient_code";
        public static final String SENDER_CODE = "sender_code";
        public static final String MESSAGE = "message";
        public static final String TYPE = "type";
        public static final String CREATED_DATE = "created_date";
        public static final String ROOM_CODE = "room_code";
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
