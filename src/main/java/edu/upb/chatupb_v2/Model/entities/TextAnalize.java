package edu.upb.chatupb_v2.Model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TextAnalize {
    private String message;

    @Override
    public String toString() {
        return "TextAnalize{" +
                "message='" + message + '\'' +
                '}';
    }
}
