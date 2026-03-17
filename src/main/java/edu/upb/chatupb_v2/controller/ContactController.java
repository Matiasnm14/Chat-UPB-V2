package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;
import edu.upb.chatupb_v2.model.repository.CacheContactDAO;
import edu.upb.chatupb_v2.model.repository.IContactDAO;
import edu.upb.chatupb_v2.view.IChatView;

import java.util.List;

public class ContactController {
    private final IContactDAO contactDao;
    private final IChatView iChatView;

    public ContactController(IChatView iChatView) {
        this.contactDao = new CacheContactDAO();
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

    public void deleteByCode(String code) throws Exception {
        contactDao.deleteByCode(code);
    }

    public void updateThemeByCode(String code, String themeId) throws Exception {
        contactDao.updateThemeByCode(code, themeId);
    }

    public void deleteAll() throws Exception {
        contactDao.deleteAll();
    }
}
