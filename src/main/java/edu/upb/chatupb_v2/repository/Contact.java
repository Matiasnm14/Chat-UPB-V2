package edu.upb.chatupb_v2.repository;

import lombok.*;

import java.io.Serializable;
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Contact implements Serializable,Model {

    private String id;
    private String name;
    private String ip;
    private String userId;

    public static final class Column {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String IP = "ip";
        public static final String USER_ID = "Users_id";
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
