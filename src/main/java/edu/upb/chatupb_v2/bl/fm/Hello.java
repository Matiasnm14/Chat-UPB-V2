package edu.upb.chatupb_v2.bl.fm;

import java.util.regex.Pattern;

public class Hello extends Message{
    String idUsuario;

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Hello(String codigo) {
        super(codigo);
    }

    @Override
    public String generarTrama() {
        return getCodigo()+Pattern.quote("|")+idUsuario+System.lineSeparator();
    }

    public static Hello parse(String trama) throws Exception {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 2){
            throw new Exception("No es asi, sonso!");
        }
        return new Hello(split[1]);
    }

}
