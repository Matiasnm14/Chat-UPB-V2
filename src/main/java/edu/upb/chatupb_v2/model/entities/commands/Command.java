package edu.upb.chatupb_v2.model.entities.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
public abstract class Command {
    @Getter
    String ID;
    public abstract String createFormat();


}
