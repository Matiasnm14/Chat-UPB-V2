package edu.upb.chatupb_v2.Model.strategy;

import edu.upb.chatupb_v2.Model.entities.Cobro;

public interface IPago {
    Cobro cobrar(double costo);
}
