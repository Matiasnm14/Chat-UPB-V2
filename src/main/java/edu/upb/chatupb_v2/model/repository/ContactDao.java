package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class ContactDao {


    private DaoHelper<AcceptHello.User.Contact> helper;

    public ContactDao() {
        helper = new DaoHelper<>();
    }

    DaoHelper.ResultReader<AcceptHello.User.Contact> resultReader = result -> {
        AcceptHello.User.Contact prefacturaSync = new AcceptHello.User.Contact();
        if (existColumn(result, AcceptHello.User.Contact.Column.ID)) {
            prefacturaSync.setId(result.getLong(AcceptHello.User.Contact.Column.ID));
        }
        if (existColumn(result, AcceptHello.User.Contact.Column.CODE)) {
            prefacturaSync.setCode(result.getString(AcceptHello.User.Contact.Column.CODE));
        }
        if (existColumn(result, AcceptHello.User.Contact.Column.NAME)) {
            prefacturaSync.setName(result.getString(AcceptHello.User.Contact.Column.NAME));
        }
        if (existColumn(result, AcceptHello.User.Contact.Column.IP)) {
            prefacturaSync.setIp(result.getString(AcceptHello.User.Contact.Column.IP));
        }
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

    public List<AcceptHello.User.Contact> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM contact";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM contact WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM contact WHERE code='" + code + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public AcceptHello.User.Contact findByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT * FROM contact WHERE code ='" + code + "'";
        System.out.println(query);
        List<AcceptHello.User.Contact> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(AcceptHello.User.Contact contact) throws Exception {
        String query = "INSERT INTO contact(code, name, ip) values (?,?,?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, contact.getCode());
                pst.setString(2, contact.getName());
                pst.setString(3, contact.getIp());
            }
        };
        helper.insert(query, params, contact);
    }

    public void update(AcceptHello.User.Contact contact) throws Exception {
        String query = "UPDATE contact SET IP=? WHERE code =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, contact.getIp());
                pst.setString(2, contact.getCode());
            }
        };
        helper.update(query, params);
    }

    public void deleteByCode(String code) throws Exception {
        String query = "DELETE FROM contact WHERE code = ?";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, code);
        helper.update(query, params);
    }

    public void update(String query, String conditionWhere) throws SQLException, ConnectException {
        if (query.trim().endsWith("%s")) {
            query = String.format(query, conditionWhere);
        } else {
            query = String.format("%s %s", query, conditionWhere);
        }
        helper.update(query, null);
    }

    public void deleteAll() throws Exception {
        String query = "CONTACTO ELIMINADO";
        helper.update(query, null);
    }
}
