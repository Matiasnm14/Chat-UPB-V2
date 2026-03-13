package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.Contact;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheContactDAO implements IContactDao{

    private Map<String, Contact> contacts = new HashMap<>();

    private final IContactDao contactDao;


    public CacheContactDAO(IContactDao contactDao) {
        this.contactDao = contactDao;
    }

    @Override
    public List<Contact> findAll() throws ConnectException, SQLException {
        if (contacts.isEmpty()){
            List<Contact> contactList = contactDao.findAll();
            for (Contact contact : contactList) contacts.putIfAbsent(contact.getId(), contact);
        }
        return contacts.values().stream().toList();
    }

    @Override
    public boolean exist(String argument) throws ConnectException, SQLException {
        return contactDao.exist(argument);
    }

    @Override
    public boolean existByCode(String code) throws ConnectException, SQLException {
        return contactDao.existByCode(code);
    }

    @Override
    public Contact findById(String id) throws ConnectException, SQLException {
        return contactDao.findById(id);
    }

    @Override
    public List<Contact> findByOwner(String ownerId) throws ConnectException, SQLException {
        return contactDao.findByOwner(ownerId);
    }

    @Override
    public Contact findByName(String name) throws ConnectException, SQLException {

        return contactDao.findByName(name);
    }

    @Override
    public void update(String query) throws Exception {
        contactDao.update(query);
        contacts.clear();
        findAll();
    }

    @Override
    public void save(Contact contact) throws Exception {
        contacts.putIfAbsent(contact.getId(), contact);
        contactDao.save(contact);
    }

    @Override
    public void updateContact(String id_contact, String ip_contact) throws Exception {
        if (contacts.containsKey(id_contact)){
            contacts.get(id_contact).setIp(ip_contact);
        } else if (existByCode(id_contact)) {
            contacts.putIfAbsent(id_contact, findById(id_contact));
        }
        contactDao.updateContact(id_contact, ip_contact);
    }

    @Override
    public void update(String query, String conditionWhere) throws SQLException, ConnectException {
        contactDao.update(query, conditionWhere);
        contacts.clear();
        findAll();
    }
}
