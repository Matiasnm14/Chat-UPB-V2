package edu.upb.chatupb_v2.model.entities.analisis;

import java.util.regex.Pattern;

public class MathAnalyzer implements TextAnalyzer{

    @Override
    public String analizar(String message) {
        String[] palabras = message.split(" ");
        for (int i = 0; i < palabras.length; i++) {
            String[] partes = palabras[i].split(Pattern.quote("+"));
            int numero1 = 0;
            try {
                numero1 = Integer.parseInt(partes[0]);
            }catch (Exception e){
                continue;
            }
            int numero2 = 0;
            try {
                numero2 = Integer.parseInt(partes[1].split(Pattern.quote("="))[0]);
            }catch (Exception e){
                continue;
            }
            int res = numero1 + numero2;
            palabras[i] += res;

        }
        message = "";
        for (String palabra : palabras) {
            message += palabra + " ";
        }
        return message;
    }
}
