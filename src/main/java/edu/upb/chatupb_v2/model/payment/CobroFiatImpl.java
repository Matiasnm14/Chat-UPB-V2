package edu.upb.chatupb_v2.model.payment;

import java.math.BigDecimal;
import java.util.Random;

public class CobroFiatImpl implements ICobro{
    private String qr;

    private BigDecimal importe;
    @Override
    public Cobro cobrar(BigDecimal importe) {
        qr = "Codigo QR a un banco Aqui";
        this.importe = importe;
        return new Cobro(qr, this.importe, null);
    }
}
