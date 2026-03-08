package edu.upb.chatupb_v2.model.payment;

import java.math.BigDecimal;

public class CobroCriptoImpl implements ICobro {
    private String qr;

    private BigDecimal importe;

    private String red;
    @Override
    public Cobro cobrar(BigDecimal importe) {
        qr = "Codigo QR a una billetera virtual Aqui";
        this.importe = importe;
        red = "Base|Poligon";
        return new Cobro(qr, this.importe, red);
    }
}
