package edu.upb.chatupb_v2.bl.fm;

import java.util.regex.Pattern;

public class Aceptar extends Message{
    String idUsuario;
    String nombre;

    public Aceptar(String idUsuario, String nombre) {
        super("002");
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    @Override
    public String generarTrama() {
        return getCodigo()+Pattern.quote("|")+idUsuario+Pattern.quote("|")+nombre+System.lineSeparator();
    }

    public static Aceptar parse(String trama) throws Exception {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 3){
            throw new Exception("No es asi, sonso!");
        }
        return new Aceptar(split[1], split[2]);
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
