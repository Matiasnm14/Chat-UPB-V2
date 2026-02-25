package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.view.IChatView;
import edu.upb.chatupb_v2.view.JUi;

public class ContactController{
    private ContactDao contactDao;
    private IChatView iChatView;

    public ContactController(IChatView view){
        this.contactDao = ContactDao.getInstance();
        this.iChatView = view;
    }
    public void onLoadContacts(){
        try {
            java.util.List<Contact> contacts = contactDao.findAll();
            iChatView.onLoadContacts(contacts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
