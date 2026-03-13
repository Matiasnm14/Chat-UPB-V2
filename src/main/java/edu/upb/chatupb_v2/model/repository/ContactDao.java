package edu.upb.chatupb_v2.model.repository;


import edu.upb.chatupb_v2.model.entities.Contact;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ContactDao implements IContactDao{
    private DaoHelper<Contact> helper;
//    private static final IContactDao uDao = new ContactDao();

//    public static IContactDao getInstance() {
//        return new ;
//    }

    public ContactDao() {
        helper = new DaoHelper<>();
    }

    DaoHelper.ResultReader<Contact> resultReader = result -> {
        Contact contact = new Contact();
        if (existColumn(result, Contact.Column.ID)) {
            contact.setId(result.getString(Contact.Column.ID));
        }
        if (existColumn(result, Contact.Column.NAME)) {
            contact.setName(result.getString(Contact.Column.NAME));
        }
        if (existColumn(result, Contact.Column.IP)) {
            contact.setIp(result.getString(Contact.Column.IP));
        }
        if (existColumn(result, Contact.Column.USER_ID)) {
            contact.setUserId(result.getString(Contact.Column.USER_ID));
        }
        return contact;
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
    @Override
    public List<Contact> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM Contacts";
        return helper.executeQuery(query, resultReader);
    }
    @Override
    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Contacts WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }
    @Override
    public boolean existByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Contacts WHERE id='" + code + "'";
        return helper.executeQueryCount(query, null) == 1;
    }
    @Override
    public Contact findById(String id) throws ConnectException, SQLException {
        String query = "SELECT * FROM Contacts WHERE id ='" + id + "'";
        System.out.println(query);
        List<Contact> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    @Override
    public List<Contact> findByOwner(String ownerId) throws ConnectException, SQLException {
        String query = "SELECT * FROM Contacts WHERE Users_id = '" + ownerId + "'";
        return helper.executeQuery(query, resultReader);
    }
    @Override
    public Contact findByName(String name) throws ConnectException, SQLException {
        String query = "SELECT * FROM Contacts WHERE name ='" + name + "'";
        System.out.println(query);
        List<Contact> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    @Override
    public void update(String query) throws Exception {
        helper.update(query, null);
    }
    @Override
    public void save(Contact contact) throws Exception {
        if (!exist("id='" + contact.getId() + "'")) {
            String query = "INSERT INTO Contacts(id, name, ip, Users_id) values (?,?,?,?)";

            DaoHelper.QueryParameters params = pst -> {
                pst.setString(1, contact.getId());
                pst.setString(2, contact.getName());
                pst.setString(3, contact.getIp());     // Nueva columna
                pst.setString(4, contact.getUserId()); // Nueva columna
            };
            helper.insert(query, params, contact);
        }
    }
    @Override
    public void updateContact(String id_contact, String ip_contact) throws Exception {
        String query = "UPDATE Contacts SET ip=? WHERE id =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, ip_contact);
                pst.setString(2, id_contact);
            }
        };
        helper.update(query, params);
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
    @Override
    public void update(String query, String conditionWhere) throws SQLException, ConnectException {
        if (query.trim().endsWith("%s")) {
            query = String.format(query, conditionWhere);
        } else {
            query = String.format("%s %s", query, conditionWhere);
        }
        helper.update(query, null);
    }
}
