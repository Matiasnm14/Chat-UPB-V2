package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.CacheContactDao;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.model.repository.IContactDao;
import edu.upb.chatupb_v2.view.IChatView;
import edu.upb.chatupb_v2.view.JUi;

public class ContactController{
    private IContactDao contactDao;
    private IChatView iChatView;

    public ContactController(IChatView view){
        this.contactDao = new CacheContactDao(ContactDao.getInstance());
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
