package edu.upb.chatupb_v2.repository;


import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAO {
    private DaoHelper<User> helper;

    public UserDAO() {
        helper = new DaoHelper<>();
    }

    DaoHelper.ResultReader<User> resultReader = result -> {
        User prefacturaSync = new User();
        if (existColumn(result, User.Column.ID)) {
            prefacturaSync.setId(result.getString(User.Column.ID));
        }
        if (existColumn(result, User.Column.NAME)) {
            prefacturaSync.setName(result.getString(User.Column.NAME));
        }
//        if (existColumn(result, User.Column.STATUSUSER)) {
//            switch (result.getString(User.Column.STATUSUSER).toLowerCase()){
//                case "online":
//                    prefacturaSync.setStatusUser(StatusUser.ONLINE);
//                    break;
//                case "offline":
//                    prefacturaSync.setStatusUser(StatusUser.OFFLINE);
//            }
//
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

    public List<User> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM Users";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Users WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Users WHERE code='" + code + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public User findById(String id) throws ConnectException, SQLException {
        String query = "SELECT * FROM Users WHERE id ='" + id + "'";
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

    public void save(User user) throws Exception {
        String query = "INSERT INTO Users(id, name) values (?,?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                System.out.println("ID: " + user.getId());
                System.out.println("NAME: " + user.getName());
                pst.setString(1, user.getId());
                pst.setString(2, user.getName());
            }
        };
        helper.insert(query, params, user);
    }

//    public void update(User user) throws Exception {
//        String query = "UPDATE user SET status_user=? WHERE id =?";
//        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
//            @Override
//            public void setParameters(PreparedStatement pst) throws SQLException {
//                pst.setString(1, user.getStatusUser().toString());
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
