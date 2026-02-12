package edu.upb.chatupb_v2.bl.fm;

import lombok.Getter;
import lombok.Setter;

public abstract class Message{
    private String codigo;

    public String getCodigo(){
        return this.codigo;
    }

    public Message(String codigo){
        this.codigo = codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public abstract String generarTrama();
}
