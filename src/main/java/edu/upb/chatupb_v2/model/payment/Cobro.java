package edu.upb.chatupb_v2.model.payment;

import edu.upb.chatupb_v2.model.entities.Model;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter

public class Cobro {
    private final String qr;
    private final BigDecimal importe;
    private final String red;

    public Cobro(String qr, BigDecimal importe, String red) {
        this.qr = qr;
        this.importe = importe;
        this.red = red;
    }
}
