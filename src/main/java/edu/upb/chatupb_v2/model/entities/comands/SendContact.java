package edu.upb.chatupb_v2.model.entities.comands;

import lombok.Getter;

import java.util.regex.Pattern;

public class SendContact extends Command{
    @Getter
    private String idUser;
    @Getter
    private String nombre;
    @Getter
    private String ip;


    @Override
    public String createFormat() {
        return getID()+"|"+getIdUser()+"|"+ getNombre()+"|"+ getIp()+System.lineSeparator();
    }

    public SendContact() {
        super("020");
    }
    public SendContact(String idUser,String nombre, String ip){
        super("020");
        this.nombre = nombre;
        this.idUser = idUser;
        this.ip = ip;
    }
    public static SendContact parse(String command){
        String[] parses = command.split(Pattern.quote("|"));
        if(parses.length != 4){
            throw new IllegalArgumentException("Formato de trama erroneo");
        }
        return new SendContact(parses[1],parses[2],parses[3]);
    }
}
