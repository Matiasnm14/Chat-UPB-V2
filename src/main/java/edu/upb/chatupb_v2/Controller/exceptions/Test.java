package edu.upb.chatupb_v2.Controller.exceptions;

import edu.upb.chatupb_v2.Controller.TextAnalizeController;
import edu.upb.chatupb_v2.Model.strategy.ITextAnalizer;
import edu.upb.chatupb_v2.Model.strategy.TextAnalizeMath;
import edu.upb.chatupb_v2.Model.strategy.TextAnalizeWord;

public class Test {
    public static void main(String[] args) {
        String suma = "hola";
        TextAnalizeController textAnalizeController = new TextAnalizeController();
        System.out.println(textAnalizeController.analizarTexto(suma));;
    }
}
