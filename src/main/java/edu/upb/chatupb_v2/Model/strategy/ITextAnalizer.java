package edu.upb.chatupb_v2.Model.strategy;

import edu.upb.chatupb_v2.Model.entities.TextAnalize;

public interface ITextAnalizer {
    TextAnalize revisarTexto(String message);
}
