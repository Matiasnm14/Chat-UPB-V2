package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.view.IChatView;

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
}
