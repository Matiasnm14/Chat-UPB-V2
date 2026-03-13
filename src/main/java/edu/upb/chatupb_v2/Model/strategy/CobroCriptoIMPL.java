package edu.upb.chatupb_v2.Model.strategy;

import edu.upb.chatupb_v2.Model.entities.Cobro;

public class CobroCriptoIMPL implements IPago {
    @Override
    public Cobro cobrar(double costo) {
        return new Cobro("QR GENERADO - CRIPTO", 1239.1,"POLYGON");

    }
}
