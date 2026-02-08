package edu.upb.chatupb_v2.repository;

import edu.upb.chatupb_v2.repository.enums.StatusUser;
import lombok.*;

import java.io.Serializable;
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable,Model {

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
        return 0;
    }
}
