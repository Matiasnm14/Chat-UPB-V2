package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.User;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.sql.*;
import java.util.List;



@Slf4j
public class UserDao {


    private DaoHelper<User> helper;
    private final String URL = "jdbc:sqlite:chat_upb_v2.sqlite";
    private static final UserDao userDao = new UserDao();
    public static UserDao getInstance(){
        return userDao;
    }

    public UserDao() {
        helper = new DaoHelper<>();
    }

    DaoHelper.ResultReader<User> resultReader = result -> {
        User prefacturaSync = new User();
        if (existColumn(result, User.Column.ID)) {
            prefacturaSync.setId(result.getString(User.Column.ID));
        }
//        if (existColumn(result, User.Column.CODE)) {
//            prefacturaSync.setCode(result.getString(User.Column.CODE));
//        }
        if (existColumn(result, User.Column.NAME)) {
            prefacturaSync.setName(result.getString(User.Column.NAME));
        }
//        if (existColumn(result, User.Column.IP)) {
//            prefacturaSync.setIp(result.getString(User.Column.IP));
//        }
        return prefacturaSync;
    };

    public static boolean existColumn(ResultSet result, String columnName) {
        try {
            result.findColumn(columnName);
            return true;
        } catch (SQLException sqlex) {
            //log.error("No se encontro la columna: {}", columnName); // log innecesario
        }
        return false;
    }
    public User getMyUser() {
        String sql = "SELECT id, name FROM Users LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                // Retornamos el usuario encontrado
                return new User(rs.getString("id"), rs.getString("username"));
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar el usuario: " + e.getMessage());
        }
        return null; // No hay usuario
    }

    public List<User> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM Users";
        return helper.executeQuery(query, resultReader);
    }
    public User findByName(String name) throws ConnectException, SQLException {
        String query = "SELECT * FROM Users WHERE name ='" + name + "'";
        System.out.println(query);
        List<User> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Users WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Users WHERE code='" + code + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public User findByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT * FROM Users WHERE code ='" + code + "'";
        System.out.println(query);
        List<User> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(User contact) throws Exception {
        if(!exist("id='" + contact.getId() + "'")){


        String query = "INSERT INTO Users(id, name) values (?,?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, contact.getId());
                pst.setString(2, contact.getName());
//                pst.setString(3, contact.getIp());
            }
        };
        helper.insert(query, params, contact);
    }
    }

//    public void update(User contact) throws Exception {
//        String query = "UPDATE Users SET IP=? WHERE code =?";
//        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
//            @Override
//            public void setParameters(PreparedStatement pst) throws SQLException {
//                pst.setString(1, contact.getIp());
//                pst.setString(2, contact.getCode());
//            }
//        };
//        helper.update(query, params);
//    }

    public void update(String query, String conditionWhere) throws SQLException, ConnectException {
        if (query.trim().endsWith("%s")) {
            query = String.format(query, conditionWhere);
        } else {
            query = String.format("%s %s", query, conditionWhere);
        }
        helper.update(query, null);
    }
}
