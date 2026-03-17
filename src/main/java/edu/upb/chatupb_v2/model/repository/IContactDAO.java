package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.List;

public interface IContactDAO {
    List<AcceptHello.User.Contact> findAll() throws ConnectException, SQLException;

    boolean existByCode(String code) throws ConnectException, SQLException;

    boolean existByIp(String ip) throws ConnectException, SQLException;

    AcceptHello.User.Contact findByIp(String ip) throws ConnectException, SQLException;

    AcceptHello.User.Contact findByCode(String code) throws ConnectException, SQLException;

    void updateIpByCode(String code, String ip) throws Exception;

    void updateThemeByCode(String code, String themeId) throws Exception;

    void update(String query) throws Exception;

    void save(AcceptHello.User.Contact contact) throws Exception;

    void deleteByCode(String code) throws Exception;

    void deleteAll() throws Exception;
}
