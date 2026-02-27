package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.Controller.ContactController;
import edu.upb.chatupb_v2.VIews.JUi;

public class Main {
    public static void main(String[] args) {
        JUi jUi = new JUi();
        ContactController conn = new ContactController(jUi);
        jUi.setController(conn);
        jUi.init();
    }
}
