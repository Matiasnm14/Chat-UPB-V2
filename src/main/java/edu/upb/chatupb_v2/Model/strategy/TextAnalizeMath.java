package edu.upb.chatupb_v2.Model.strategy;

import edu.upb.chatupb_v2.Model.entities.TextAnalize;

public class TextAnalizeMath implements ITextAnalizer{
    @Override
    public TextAnalize revisarTexto(String message) {
        int firstNum = Integer.parseInt(message.split("\\+")[0]);
        int secondNum = Integer.parseInt(message.split("\\+")[1].split("=")[0]);
        int suma = firstNum + secondNum;
        String res = message + suma;

        return new TextAnalize(res);
    }
}
