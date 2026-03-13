package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.Contact;

import javax.swing.*;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheContactDao implements IContactDao{
    private Map<String, Contact> contactChache = new HashMap<>();
    private IContactDao iContactDao;

    public CacheContactDao(IContactDao iContactDao){
        super();
        this.iContactDao = iContactDao;
    }
    @Override
    public List<Contact> findAll() throws ConnectException, SQLException {
        if(contactChache.isEmpty()){
            List<Contact> contacts = iContactDao.findAll();
            contacts.forEach(contact -> contactChache.put(contact.getId(),contact));
        }
        return contactChache.values().stream().toList();
    }
    @Override
    public boolean existByCode(String code) throws ConnectException, SQLException {
        return contactChache.containsKey(code);
    }

    @Override
    public Contact findById(String id) throws ConnectException, SQLException {
        return contactChache.get(id);
    }

    @Override
    public void update(String query, String conditionWhere) throws ConnectException, SQLException {
        iContactDao.update(query,conditionWhere);
        contactChache.clear();
        findAll();
    }

    @Override
    public void save(Contact contact) throws ConnectException, SQLException {
        iContactDao.save(contact);
        contactChache.put(contact.getId(),contact);
    }

    @Override
    public void updateIp(String idContacto, String nuevaIp) throws ConnectException, SQLException {
        iContactDao.updateIp(idContacto,nuevaIp);
        contactChache.get(idContacto).setIp(nuevaIp);
    }
}
