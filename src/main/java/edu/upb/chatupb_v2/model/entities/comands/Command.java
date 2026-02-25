package edu.upb.chatupb_v2.repository.comands;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
public abstract class Command {
    @Getter
    String ID;
    public abstract String createFormat();


}
