package edu.upb.chatupb_v2.repository;

import edu.upb.chatupb_v2.repository.enums.StatusMessage;
import edu.upb.chatupb_v2.repository.enums.TypeMessage;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
/*
    
 */
public class MessageDAO {
    private DaoHelper<Message> helper;
    
    public MessageDAO() {
        helper = new DaoHelper<>();
    }

    DaoHelper.ResultReader<Message> resultReader = result -> {
        Message prefacturaSync = new Message();
        if (existColumn(result, Message.Column.ID)) {
            prefacturaSync.setId(result.getLong(Message.Column.ID));
        }
        if (existColumn(result, Message.Column.CONTENT)) {
            prefacturaSync.setContent(result.getString(Message.Column.CONTENT));
        }
        if (existColumn(result, Message.Column.STATUSMESSAGE)) {
            switch (result.getString(Message.Column.STATUSMESSAGE).toLowerCase()){
                case "sent":
                    prefacturaSync.setStatusMessage(StatusMessage.SENT);
                    break;
                case "received":
                    prefacturaSync.setStatusMessage(StatusMessage.RECEIVED);
                    break;
                case "read":
                    prefacturaSync.setStatusMessage(StatusMessage.READ);
                    break;
                case "error":
                    prefacturaSync.setStatusMessage(StatusMessage.ERROR);

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
            prefacturaSync.setDate(result.getString(Message.Column.TYPEMESSAGE));
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
        String query = "SELECT * FROM message";
        return helper.executeQuery(query, resultReader);
    }

    public boolean exist(String argument) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM message WHERE " + argument;
        return helper.executeQueryCount(query, null) == 1;
    }

    public boolean existByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM message WHERE code='" + code + "'";
        return helper.executeQueryCount(query, null) == 1;
    }

    public Message findByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT * FROM message WHERE code ='" + code + "'";
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
        String query = "INSERT INTO message(content,date,status_message,type_message) values (?,?,?,?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, message.getContent());
                pst.setString(2, message.getDate());
                pst.setString(3, message.getStatusMessage().toString());
                pst.setString(4,message.getTypeMessage().toString());
            }
        };
        helper.insert(query, params, message);
    }

    public void update(Message message) throws Exception {
        String query = "UPDATE message SET status_message=? WHERE id =?";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, message.getStatusMessage().toString());
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
