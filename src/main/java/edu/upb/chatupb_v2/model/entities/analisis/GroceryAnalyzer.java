package edu.upb.chatupb_v2.model.entities.analisis;

import java.util.ArrayList;
import java.util.List;

public class GroceryAnalyzer implements TextAnalyzer{

    private List<String> grocerias = new ArrayList<>();

    public GroceryAnalyzer() {
        grocerias.add("puta");
        grocerias.add("mierda");
        grocerias.add("carajo");
        grocerias.add("pene");
        grocerias.add("vagina");
    }

    @Override
    public String analizar(String message) {
        String[] palabras = message.split(" ");
        for (int i = 0; i < palabras.length; i++) {
            if (grocerias.contains(palabras[i].toLowerCase())) {
                int letras = palabras[i].length();
                palabras[i] = "";
                for (int j = 0; j < letras; j++) {
                    palabras[i] += "*";
                }
            }
        }
        message = "";
        for (String palabra : palabras) {
            message += palabra + " ";
        }
        return message;
    }
}
