package edu.upb.chatupb_v2.bl.fm;

public class AceptHello extends Message{
    String id;

    public AceptHello(String codigo) {
        super(codigo);
    }

    public Message parse(String trama) {
        return null;
    }

    @Override
    public String generarTrama() {
        return "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
