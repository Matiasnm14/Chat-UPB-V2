package edu.upb.chatupb_v2.view;

import edu.upb.chatupb_v2.controller.ContactController;

public class ClearContacts {
    public static void main(String[] args) {
        ContactController controller = new ContactController(null);
        try {
            controller.deleteAll();
            System.out.println("Contactos eliminados.");
        } catch (Exception e) {
            System.out.println("Error eliminando contactos: " + e.getMessage());
        }
    }
}
