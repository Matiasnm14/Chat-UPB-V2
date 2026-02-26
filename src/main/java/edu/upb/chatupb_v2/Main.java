package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.controller.ContactController;
import edu.upb.chatupb_v2.view.JUi;

public class Main {
    public static void main(String[] args) {
        JUi jUi = new JUi();
        ContactController contact = new ContactController(jUi);
        contact.unload();
        jUi.init();
    }
}
//HORA, NOMBRE,ICONO
