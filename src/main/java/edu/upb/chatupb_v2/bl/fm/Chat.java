package edu.upb.chatupb_v2.bl.fm;

public class Chat extends Message{
    String idUsuario;

    public Chat(String codigo) {
        super(codigo);
    }

    @Override
    public String generarTrama() {
        return "";
    }


    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
