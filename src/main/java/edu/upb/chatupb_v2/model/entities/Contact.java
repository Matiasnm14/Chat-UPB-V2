package edu.upb.chatupb_v2.model.entities;

import lombok.*;

import java.io.Serializable;
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Contact implements Serializable, Model {

    private String id;
    private String name;
    private String ip;
    private String userId;
    private boolean stateConnect = false;
    private boolean buzz = false;
    private String id_pin;
    private String theme;

    public static final class Column {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String IP = "ip";
        public static final String USER_ID = "Users_id";
        public static final String ID_PIN = "id_pin";
        public static final String THEME = "theme";
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
    public boolean isStateConnect() {
        return stateConnect;
    }

    public void setStateConnect(boolean stateConnect) {
        this.stateConnect = stateConnect;
    }
}
