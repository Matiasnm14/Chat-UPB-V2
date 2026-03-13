package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.Contact;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface IContactDao {

    List<Contact> findAll() throws ConnectException, SQLException;

    boolean exist(String argument) throws ConnectException, SQLException;

    boolean existByCode(String code) throws ConnectException, SQLException;

    Contact findById(String id) throws ConnectException, SQLException;

    List<Contact> findByOwner(String ownerId) throws ConnectException, SQLException;

    Contact findByName(String name) throws ConnectException, SQLException;

    void update(String query) throws Exception;

    void save(Contact contact) throws Exception;

    void updateContact(String id_contact, String ip_contact) throws Exception;

    void update(String query, String conditionWhere) throws SQLException, ConnectException;
}
