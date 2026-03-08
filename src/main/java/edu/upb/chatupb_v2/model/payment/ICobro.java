package edu.upb.chatupb_v2.model.payment;

import java.math.BigDecimal;

public interface ICobro {
    public Cobro cobrar(BigDecimal importe);
}
