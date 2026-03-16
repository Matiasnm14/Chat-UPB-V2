package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.Message;
import edu.upb.chatupb_v2.model.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.model.repository.enums.TypeMessage;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
/*
    
 */
public class MessageDAO {
    private DaoHelper<Message> helper;
    private static final MessageDAO mDao = new MessageDAO();

    public static MessageDAO getInstance(){
        return mDao;
    }

    private MessageDAO(){
        helper = new DaoHelper<>();
    }

    DaoHelper.ResultReader<Message> resultReader = result -> {
        Message msg = new Message();
        if (existColumn(result, Message.Column.ID_MESSAGE)) {
            msg.setIdMessage(result.getString(Message.Column.ID_MESSAGE));
        }
        if (existColumn(result, Message.Column.CONTACT_ID)) {
            msg.setContactId(result.getString(Message.Column.CONTACT_ID));
        }
        if (existColumn(result, Message.Column.BODY)) {
            msg.setBody(result.getString(Message.Column.BODY));
        }
        if (existColumn(result, Message.Column.STATUS)) {
            String status = result.getString(Message.Column.STATUS);
            if (status != null) msg.setStatusMessage(StatusMessage.valueOf(status.toUpperCase()));
        }
        if (existColumn(result, Message.Column.TYPE)) {
            String type = result.getString(Message.Column.TYPE);
            if (type != null) msg.setTypeMessage(TypeMessage.valueOf(type.toUpperCase()));
        }
        if (existColumn(result, Message.Column.DATE)) {
            msg.setDate(result.getString(Message.Column.DATE));
        }
        return msg;
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

    public List<Message> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM Messages";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Messages WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existById(String id) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Messages WHERE id='" + id + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public Message findById(String id) throws ConnectException, SQLException {
        String query = "SELECT * FROM Messages WHERE id ='" + id + "'";
        System.out.println(query);
        List<Message> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    public List<Message> findByContact(String contactId) throws ConnectException, SQLException {
        String query = "SELECT * FROM Messages WHERE Contacts_id = '" + contactId + "' ORDER BY date ASC";
        return helper.executeQuery(query, resultReader);
    }

    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(Message message) throws Exception {
        String query = "INSERT INTO Messages(id, Contacts_id, message, date, type, status) values (?,?,?,?,?,?)";

        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, message.getIdMessage());
            pst.setString(2, message.getContactId());
            pst.setString(3, message.getBody());
            pst.setString(4, message.getDate());
            pst.setString(5, message.getTypeMessage().toString());
            pst.setString(6, message.getStatusMessage().toString());
        };
        helper.insert(query, params, message);
    }

    public void updateMessage(String id_message) throws Exception {
        String query = "UPDATE Messages SET status=? WHERE id =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, StatusMessage.READ.toString());
                pst.setString(2, id_message);
            }
        };
        helper.update(query, params);
    }

    public void updateMessageReceived(String id_message) throws Exception {
        String query = "UPDATE Messages SET status=? WHERE id =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, StatusMessage.RECEIVED.toString());
                pst.setString(2, id_message);
            }
        };
        helper.update(query, params);
    }

    public void updateUniqueMessage(String id_message) throws Exception {
        String query = "UPDATE Messages SET message='Mensaje Abierto' WHERE id =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, id_message);
            }
        };
        helper.update(query, params);
        query = "UPDATE Messages SET type='TEXT' WHERE id =?";
        params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, id_message);
            }
        };
        helper.update(query, params);
    }

    public void delete(String id_message) throws Exception {
        String query = "DELETE FROM Messages WHERE id =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, id_message);
            }
        };
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
}
