package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.Contact;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface IContactDao {
    List<Contact> findAll()throws ConnectException, SQLException ;
    boolean existByCode(String code)throws ConnectException, SQLException ;
    Contact findById(String id)throws ConnectException, SQLException ;
    void update(String query, String conditionWhere)throws ConnectException, SQLException ;
    void save(Contact contact)throws ConnectException, SQLException ;
    void updateIp(String idContacto, String nuevaIp)throws ConnectException, SQLException ;


    }
