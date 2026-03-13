package edu.upb.chatupb_v2.Model.repository;

import edu.upb.chatupb_v2.Model.entities.Message;
import edu.upb.chatupb_v2.Model.entities.comands.Image;
import edu.upb.chatupb_v2.Model.entities.enums.StatusMessage;
import edu.upb.chatupb_v2.Model.entities.enums.TypeMessage;

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
        Message prefacturaSync = new Message();
        if (existColumn(result, Message.Column.ID_MESSAGE)) {
            prefacturaSync.setIdMessage(result.getString(Message.Column.ID_MESSAGE));
        }
        if (existColumn(result, Message.Column.SENDER_ID)) {
            prefacturaSync.setSendUser(result.getString(Message.Column.SENDER_ID));
        }
        if (existColumn(result, Message.Column.RECEIVER_ID)) {
            prefacturaSync.setReceiveUser(result.getString(Message.Column.RECEIVER_ID));
        }
        if (existColumn(result, Message.Column.BODY)) {
            prefacturaSync.setBody(result.getString(Message.Column.BODY));
        }
        if (existColumn(result, Message.Column.STATUSMESSAGE)) {
            switch (result.getString(Message.Column.STATUSMESSAGE).toLowerCase()){
                case "sent":
                    prefacturaSync.setStatusMessage(StatusMessage.SENT);
                    break;
//                case "received":
//                    prefacturaSync.setStatusMessage(StatusMessage.RECEIVED);
//                    break;
                case "read":
                    prefacturaSync.setStatusMessage(StatusMessage.READ);
                    break;
//                case "error":
//                    prefacturaSync.setStatusMessage(StatusMessage.ERROR);

            }

        }
        if (existColumn(result, Message.Column.TYPEMESSAGE)) {
            switch (result.getString(Message.Column.TYPEMESSAGE).toLowerCase()){
                case "text":
                    prefacturaSync.setTypeMessage(TypeMessage.TEXT);
                    break;
                case "image":
                    prefacturaSync.setTypeMessage(TypeMessage.IMAGE);
            }
        }
        if (existColumn(result, Message.Column.DATE)) {
            prefacturaSync.setDate(result.getString(Message.Column.DATE));
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

    public List<Message> findAll() throws ConnectException, SQLException {
        String query = "SELECT * FROM Messages";
        return helper.executeQuery(query, resultReader);
    }

    public List<Message> getConversation(String user1Id, String user2Id)
            throws ConnectException, SQLException {

        String query = """
        SELECT id_message, sender_id, receiver_id, body, type_message, date FROM Messages
        WHERE ((sender_id = ?) AND (receiver_id = ?))
           OR ((sender_id = ?) AND (receiver_id = ?))
        ORDER BY date ASC
    """;

        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, user1Id);
            pst.setString(2, user2Id);
            pst.setString(3, user2Id);
            pst.setString(4, user1Id);
        };

        return helper.executeQuery(query, params, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Messages WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existById(String id) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM Messages WHERE id_message='" + id + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public Message findById(String id) throws ConnectException, SQLException {
        String query = "SELECT * FROM Messages WHERE id_message ='" + id + "'";
        System.out.println(query);
        List<Message> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(Message message) throws Exception {
        String query = "INSERT INTO Messages(id_message, sender_id, receiver_id, body,type_message,status_message, date) values (?,?,?,?,?,?,?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, message.getIdMessage());
                pst.setString(2, message.getSendUser());
                pst.setString(3, message.getReceiveUser());
                pst.setString(4, message.getBody());
                pst.setString(5, message.getTypeMessage().toString());
                pst.setString(6, message.getStatusMessage().toString());
                pst.setString(7, message.getDate());
            }
        };
        helper.insert(query, params, message);
    }

    public void updateMessage(String id_message) throws Exception {
        String query = "UPDATE Messages SET status_message=? WHERE id_message =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, StatusMessage.READ.toString());
                pst.setString(2, id_message);
            }
        };
        helper.update(query, params);
    }

    public void delete(String id_message) throws Exception {
        String query = "DELETE FROM Messages WHERE id_message =?";
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
