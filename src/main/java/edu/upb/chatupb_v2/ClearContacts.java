package edu.upb.chatupb_v2;

import edu.upb.chatupb_v2.repository.ContactDao;

public class ClearContacts {
    public static void main(String[] args) {
        ContactDao dao = new ContactDao();
        try {
            dao.deleteAll();
            System.out.println("Contactos eliminados.");
        } catch (Exception e) {
            System.out.println("Error eliminando contactos: " + e.getMessage());
        }
    }
}
