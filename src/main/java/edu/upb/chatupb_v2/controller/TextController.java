package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.analisis.GroceryAnalyzer;
import edu.upb.chatupb_v2.model.entities.analisis.MathAnalyzer;
import edu.upb.chatupb_v2.model.entities.analisis.TextAnalyzer;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.ArrayList;
import java.util.List;

public class TextController {

    private IChatView view;


    public TextController(IChatView view) {
        this.view = view;

    }

    public String analizarTexto(String text){
        TextAnalyzer analyzer = new GroceryAnalyzer();
        text = analyzer.analizar(text);
        analyzer = new MathAnalyzer();
        text = analyzer.analizar(text);
        return text;
    }
}
