package edu.upb.chatupb_v2.bl.fm;

import java.util.regex.Pattern;

public class Decline extends Message {

    public Decline() {
        super("003");
    }

    public static Decline parse(String trama) throws Exception {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 3){
            throw new Exception("No es asi, sonso!");
        }
        return new Decline();
    }

    @Override
    public String generarTrama() {
        return getCodigo()+Pattern.quote("|")+System.lineSeparator();
    }
}
