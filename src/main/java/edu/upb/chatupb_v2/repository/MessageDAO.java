package edu.upb.chatupb_v2.repository;

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
        if (existColumn(result, Message.Column.COD_MESSAGE)) {
            prefacturaSync.setCodMessage(result.getString(Message.Column.COD_MESSAGE));
        }
        if (existColumn(result, Message.Column.RECIPIENT_CODE)) {
            prefacturaSync.setRecipientCode(result.getString(Message.Column.RECIPIENT_CODE));
        }
        if (existColumn(result, Message.Column.SENDER_CODE)) {
            prefacturaSync.setSenderCode(result.getString(Message.Column.SENDER_CODE));
        }
        if (existColumn(result, Message.Column.MESSAGE)) {
            prefacturaSync.setMessage(result.getString(Message.Column.MESSAGE));
        }
        if (existColumn(result, Message.Column.TYPE)) {
            String typeValue = result.getString(Message.Column.TYPE);
            if (typeValue != null) {
                switch (typeValue.toLowerCase()) {
                    case "text":
                        prefacturaSync.setType(TypeMessage.TEXT);
                        break;
                    case "image":
                        prefacturaSync.setType(TypeMessage.IMAGE);
                        break;
                }
            }
        }
        if (existColumn(result, Message.Column.CREATED_DATE)) {
            prefacturaSync.setCreatedDate(result.getString(Message.Column.CREATED_DATE));
        }
        if (existColumn(result, Message.Column.ROOM_CODE)) {
            prefacturaSync.setRoomCode(result.getString(Message.Column.ROOM_CODE));
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
        String query = "SELECT * FROM message ORDER BY id ASC";
        return helper.executeQuery(query, resultReader);
    }

    public List<Message> findByParticipants(String userCode, String contactCode) throws ConnectException, SQLException {
        String query = "SELECT * FROM message WHERE (sender_code = ? AND recipient_code = ?) OR (sender_code = ? AND recipient_code = ?) ORDER BY id ASC";
        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, userCode);
            pst.setString(2, contactCode);
            pst.setString(3, contactCode);
            pst.setString(4, userCode);
        };
        return helper.executeQuery(query, params, resultReader);
    }

    public List<Message> findByRoomCode(String roomCode) throws ConnectException, SQLException {
        String query = "SELECT * FROM message WHERE room_code = ? ORDER BY id ASC";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, roomCode);
        return helper.executeQuery(query, params, resultReader);
    }

    public void save(Message message) throws Exception {
        String query = "INSERT INTO message(cod_message, recipient_code, created_date, sender_code, message, type, room_code) values (?,?,?,?,?,?,?)";
        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, message.getCodMessage());
            pst.setString(2, message.getRecipientCode());
            pst.setString(3, message.getCreatedDate());
            pst.setString(4, message.getSenderCode());
            pst.setString(5, message.getMessage());
            pst.setString(6, message.getType() != null ? message.getType().toString() : TypeMessage.TEXT.toString());
            pst.setString(7, message.getRoomCode());
        };
        helper.insert(query, params, message);
    }
}