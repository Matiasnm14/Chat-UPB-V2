package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.CacheContactDAO;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.model.repository.IContactDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.net.ConnectException;
import java.sql.SQLException;

public class ContactController {
    private IContactDao contactDao;

    private IChatView view;

    public ContactController(IChatView view){
        contactDao = new CacheContactDAO(new ContactDao());
        this.view = view;
    }

    public void onLoadContacts(){
        try {

            java.util.List<Contact> contacts = contactDao.findAll();
            view.onLoadContacts(contacts);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(Contact contact) throws Exception {
        contactDao.save(contact);
    }

    public boolean existByCode(String code) throws ConnectException, SQLException {
        return contactDao.existByCode(code);
    }


    public Contact findById(String id) throws ConnectException, SQLException {
        return contactDao.findById(id);
    }

    public void updateContact(String id_contact, String ip_contact) throws Exception {
        contactDao.updateContact(id_contact, ip_contact);
    }
}
