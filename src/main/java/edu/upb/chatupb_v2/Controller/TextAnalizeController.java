package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Model.entities.TextAnalize;
import edu.upb.chatupb_v2.Model.strategy.ITextAnalizer;
import edu.upb.chatupb_v2.Model.strategy.TextAnalizeMath;
import edu.upb.chatupb_v2.Model.strategy.TextAnalizeWord;

public class TextAnalizeController {
    public String analizarTexto(String message){
        ITextAnalizer iTextAnalizer = null;
        StringBuilder newMessage = new StringBuilder();
        String[] messages = message.split(" ");

        for (String s : messages) {
            if (s.contains("+") && s.contains("=")){
                iTextAnalizer = new TextAnalizeMath();
                newMessage.append(iTextAnalizer.revisarTexto(s).getMessage());
            }
            else {
                iTextAnalizer = new TextAnalizeWord();
                newMessage.append(iTextAnalizer.revisarTexto(s).getMessage());
            }
        }
        return newMessage.toString();
    }
}
