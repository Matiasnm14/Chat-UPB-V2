package edu.upb.chatupb_v2.Model.repository;

import edu.upb.chatupb_v2.Model.entities.User;

import java.awt.*;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface IUserDao {
    static boolean existColumn(ResultSet result, String columnName) {
        return false;
    }

    List<User> findAll() throws ConnectException, SQLException;
    List<User> findAllContactsForUser(String id) throws ConnectException, SQLException;
    boolean exist(String argument) throws ConnectException, SQLException;
    boolean existByCode(String id) throws ConnectException, SQLException;
    User findById(String id) throws ConnectException, SQLException;
    void deleteUser(String id) throws ConnectException, SQLException;
    void update(String query) throws Exception;
    void save(User user) throws Exception;
    void update(String query, String conditionWhere) throws SQLException, ConnectException;
}
