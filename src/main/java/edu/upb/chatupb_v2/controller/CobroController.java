package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.payment.Cobro;
import edu.upb.chatupb_v2.model.payment.CobroCriptoImpl;
import edu.upb.chatupb_v2.model.payment.CobroFiatImpl;
import edu.upb.chatupb_v2.model.payment.ICobro;
import edu.upb.chatupb_v2.model.repository.enums.CobroType;
import edu.upb.chatupb_v2.view.IChatView;

import java.math.BigDecimal;

public class CobroController {

    private ICobro cobro;

    private IChatView view;

    public CobroController(IChatView view){
        this.view = view;
    }

    public void cobrar(BigDecimal importe, CobroType cobroType){
        cobro = (cobroType == CobroType.BOB? new CobroFiatImpl() : new CobroCriptoImpl());
        view.cobrar(cobro.cobrar(importe));
    }
}
