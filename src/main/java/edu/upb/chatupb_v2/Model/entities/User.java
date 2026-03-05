package edu.upb.chatupb_v2.Model.entities;

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
    private String ip;
    private String savedById;
    public static final class Column{
        public static final String ID= "id";
        public static final String NAME= "name";
        public static final String IP_USER = "ip_user";
        public static final String SAVED_BY_ID = "saved_by_id";
//        public static final String STATUSUSER ="status";
//        public static final String IP_ADDRESS ="ip";
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "    " + name;
    }

    @Override
    public String getId() {
        return id;
    }
}
