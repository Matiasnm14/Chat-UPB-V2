package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;
import edu.upb.chatupb_v2.model.repository.ContactDao;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.List;

public class ContactController {
    private final ContactDao contactDao;
    private final IChatView iChatView;

    public ContactController(IChatView iChatView) {
        this.contactDao = new ContactDao();
        this.iChatView = iChatView;
    }

    public void unload() {
        if (iChatView == null) {
            return;
        }
        try {
            List<AcceptHello.User.Contact> contacts = contactDao.findAll();
            iChatView.unload(contacts);
        } catch (Exception e) {
            iChatView.showError("No se pudieron cargar los contactos: " + e.getMessage());
        }
    }
}
