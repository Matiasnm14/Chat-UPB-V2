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

    private String id;
    private String name;
//    private StatusUser statusUser;
//    private String ip;

    public static final class Column{
        public static final String ID= "id";
        public static final String NAME= "name";
//        public static final String STATUSUSER ="status";
//        public static final String IP_ADDRESS ="ip";
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
