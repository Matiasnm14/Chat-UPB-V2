package edu.upb.chatupb_v2.bl.fm;


import java.util.regex.Pattern;

public class Invitacion extends Message {

    private String idUsuario;
    private String nombre;

    public Invitacion(){
        super("001");
    }

    public Invitacion(String idUsuario, String nombre){
        super("001");
        this.idUsuario = idUsuario;
        this.nombre = nombre;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public static Invitacion parse(String trama) throws Exception {
        String[] split = trama.split(Pattern.quote("|"));
        if (split.length != 3){
            throw new Exception("No es asi, sonso!");
        }
        return new Invitacion(split[1], split[2]);
    }

    @Override
    public String generarTrama() {
        return getCodigo()+Pattern.quote("|")+idUsuario+Pattern.quote("|")+nombre+System.lineSeparator();
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
