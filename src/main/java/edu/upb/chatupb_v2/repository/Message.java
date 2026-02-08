package edu.upb.chatupb_v2.repository;

import edu.upb.chatupb_v2.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.repository.enums.TypeMessage;
import lombok.*;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable, Model {

    private long id;
    private String content;
    private TypeMessage typeMessage;
    private StatusMessage statusMessage;
    private String date;
    public static final class Column{
        public static final String ID= "id";
        public static final String CONTENT ="content";
        public static final String TYPEMESSAGE ="type";
        public static final String STATUSMESSAGE = "Status";
        public static final String DATE = "date";
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return 0;
    }
}
