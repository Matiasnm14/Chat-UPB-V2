package edu.upb.chatupb_v2.Model.strategy;

import edu.upb.chatupb_v2.Model.entities.Cobro;

public class CobroFIATIMPL implements IPago{
    @Override
    public Cobro cobrar(double costo) {
        return new Cobro("QR GENERADO - FIAT", 1239.1,"");
    }
}
