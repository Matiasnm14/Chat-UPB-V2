package edu.upb.chatupb_v2.Model.strategy;

import edu.upb.chatupb_v2.Model.entities.TextAnalize;

import java.util.ArrayList;
import java.util.List;

public class TextAnalizeWord implements ITextAnalizer{
    List<String> vulgaridades = new ArrayList<>();
    int cantidad = 0;

    public void addVulgar(){
        vulgaridades.add("mierda");
        vulgaridades.add("puta");
        vulgaridades.add("carajo");
        vulgaridades.add("marica");
        vulgaridades.add("cabron");
    }
    @Override
    public TextAnalize revisarTexto(String message) {
        addVulgar();
        String [] palabras = message.split(" ");
        for (String palabra : palabras) {
            if (vulgaridades.contains(palabra)){
                cantidad = palabra.length();
            }
        }
        String [] palabras2 = message.split(",");
        for (String palabra : palabras2) {
            if (vulgaridades.contains(palabra)){
                cantidad = palabra.length();
            }
        }
        if (cantidad>0){
            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 0; i < cantidad; i++) {
                messageBuilder.insert(0, "*");
            }
            message = messageBuilder.toString();
        }
        return new TextAnalize(message);
    }
}
