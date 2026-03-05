package edu.upb.chatupb_v2.Model.entities;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable, Model {

    private String id;
    private String nombre;
    public static final class Column{
        public static final String ID_ACCOUNT= "id_account";
        public static final String NOMBRE ="nombre";
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return nombre;

    }

    @Override
    public String getId() {
        return id;
    }
}
