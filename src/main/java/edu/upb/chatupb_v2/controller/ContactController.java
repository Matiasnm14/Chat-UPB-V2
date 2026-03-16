package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.net.ConnectException;
import java.sql.SQLException;

public class ContactController {
    private ContactDao contactDao;

    private IChatView view;

    public ContactController(IChatView view){
        contactDao = ContactDao.getInstance();
        this.view = view;
    }

    public void onLoadContacts(){
        try {

            java.util.List<Contact> contacts = ContactDao.getInstance().findAll();
            view.onLoadContacts(contacts);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Contact findByName(String name) throws ConnectException, SQLException {
        return contactDao.findByName(name);
    }

    public void updatePin(String id_contact, String id_pin) throws Exception {
        contactDao.updatePin(id_contact, id_pin);
    }

    public void updateTheme(String id_contact, String theme) throws Exception {
        contactDao.updateTheme(id_contact, theme);
    }
}
