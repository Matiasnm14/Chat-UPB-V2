package edu.upb.chatupb_v2.model.entities;

import edu.upb.chatupb_v2.model.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Contact implements Serializable, Model {
    public static final String MI_ID = "af3bc20a-766c-4cd4-813d-b1067a01fa9a";

    public static final class Column {
        public static final String ID = "id";
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String IP = "ip";
        public static final String THEME_ID = "theme_id";
    }

    private long id;
    private String code;
    private String name;
    private String ip;
    private String themeId;
    private boolean stateConnect = false;

    public String roomCode() {
        return MI_ID + code;
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
