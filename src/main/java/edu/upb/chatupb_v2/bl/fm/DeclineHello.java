package edu.upb.chatupb_v2.bl.fm;

public class DeclineHello extends Message{

    public DeclineHello(String codigo) {
        super(codigo);
    }

    public Message parse(String trama) {
        return null;
    }

    @Override
    public String generarTrama() {
        return "";
    }
}
