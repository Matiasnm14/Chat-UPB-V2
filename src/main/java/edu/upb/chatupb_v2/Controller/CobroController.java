package edu.upb.chatupb_v2.Controller;


import edu.upb.chatupb_v2.Model.entities.Cobro;
import edu.upb.chatupb_v2.Model.strategy.CobroCriptoIMPL;
import edu.upb.chatupb_v2.Model.strategy.CobroFIATIMPL;
import edu.upb.chatupb_v2.Model.strategy.IPago;

public class CobroController {
    public Cobro cobrarController (int election){
        IPago pago;
        if (election == 0){
            pago = new CobroCriptoIMPL();
        } else {
            pago = new CobroFIATIMPL();
        }
        return pago.cobrar(10);
    }
}
