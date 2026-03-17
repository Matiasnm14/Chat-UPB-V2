package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.Model;
import edu.upb.chatupb_v2.model.entities.enums.StatusMessage;
import edu.upb.chatupb_v2.model.entities.enums.TypeMessage;
import lombok.*;

import java.io.Serializable;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
/*
    
 */
public class MessageDAO {
    private DaoHelper<Message> helper;

    public MessageDAO() {
        helper = new DaoHelper<>();
        ensureTable();
        ensureStatusColumn();
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
                    case "unique":
                        prefacturaSync.setType(TypeMessage.UNIQUE);
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
        if (existColumn(result, Message.Column.STATUS_MESSAGE)) {
            String statusValue = result.getString(Message.Column.STATUS_MESSAGE);
            if (statusValue != null) {
                try {
                    prefacturaSync.setStatusMessage(StatusMessage.valueOf(statusValue.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                    prefacturaSync.setStatusMessage(StatusMessage.SENT);
                }
            }
        }
        if (existColumn(result, Message.Column.PINNED)) {
            prefacturaSync.setPinned(result.getInt(Message.Column.PINNED) == 1);
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
        String query = "INSERT INTO message(cod_message, recipient_code, created_date, sender_code, message, type, room_code, status_message) values (?,?,?,?,?,?,?,?)";
        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, message.getCodMessage());
            pst.setString(2, message.getRecipientCode());
            pst.setString(3, message.getCreatedDate());
            pst.setString(4, message.getSenderCode());
            pst.setString(5, message.getMessage());
            pst.setString(6, message.getType() != null ? message.getType().toString() : TypeMessage.TEXT.toString());
            pst.setString(7, message.getRoomCode());
            pst.setString(8, message.getStatusMessage() != null ? message.getStatusMessage().toString() : StatusMessage.SENT.toString());
        };
        helper.insert(query, params, message);
    }

    public void deleteByCodeMessage(String codeMessage) throws ConnectException, SQLException {
        String query = "DELETE FROM message WHERE cod_message = ?";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, codeMessage);
        helper.update(query, params);
    }

    public void updateStatusByCodeMessage(String codeMessage, StatusMessage statusMessage) throws ConnectException, SQLException {
        String query = "UPDATE message SET status_message = ? WHERE cod_message = ?";
        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, statusMessage != null ? statusMessage.toString() : StatusMessage.SENT.toString());
            pst.setString(2, codeMessage);
        };
        helper.update(query, params);
    }

    public Message findByCodeMessage(String codeMessage) throws ConnectException, SQLException {
        String query = "SELECT * FROM message WHERE cod_message = ?";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, codeMessage);
        List<Message> list = helper.executeQuery(query, params, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void pinMessage(String codeMessage) throws ConnectException, SQLException {
        Message message = findByCodeMessage(codeMessage);
        if (message == null) {
            return;
        }
        if (message.getRoomCode() != null && !message.getRoomCode().isBlank()) {
            String clearQuery = "UPDATE message SET pinned = 0 WHERE room_code = ?";
            helper.update(clearQuery, pst -> pst.setString(1, message.getRoomCode()));
        } else {
            String clearQuery = "UPDATE message SET pinned = 0 WHERE (sender_code = ? AND recipient_code = ?) OR (sender_code = ? AND recipient_code = ?)";
            helper.update(clearQuery, pst -> {
                pst.setString(1, message.getSenderCode());
                pst.setString(2, message.getRecipientCode());
                pst.setString(3, message.getRecipientCode());
                pst.setString(4, message.getSenderCode());
            });
        }
        String query = "UPDATE message SET pinned = 1 WHERE cod_message = ?";
        helper.update(query, pst -> pst.setString(1, codeMessage));
    }

    private void ensureTable() {
        String query = "CREATE TABLE IF NOT EXISTS message (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cod_message TEXT," +
                "recipient_code TEXT," +
                "created_date TEXT," +
                "sender_code TEXT," +
                "message TEXT," +
                "type TEXT," +
                "room_code TEXT," +
                "status_message TEXT DEFAULT 'SENT'," +
                "pinned INTEGER DEFAULT 0" +
                ")";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.execute();
        } catch (Exception ignored) {
        }
    }

    private void ensureStatusColumn() {
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement info = conn.prepareStatement("PRAGMA table_info(message)");
             ResultSet rs = info.executeQuery()) {
            boolean statusExists = false;
            boolean pinnedExists = false;
            while (rs.next()) {
                String column = rs.getString("name");
                if (Message.Column.STATUS_MESSAGE.equalsIgnoreCase(column)) {
                    statusExists = true;
                }
                if (Message.Column.PINNED.equalsIgnoreCase(column)) {
                    pinnedExists = true;
                }
            }
            try (Statement alter = conn.createStatement()) {
                if (!statusExists) {
                    alter.execute("ALTER TABLE message ADD COLUMN status_message TEXT DEFAULT 'SENT'");
                }
                if (!pinnedExists) {
                    alter.execute("ALTER TABLE message ADD COLUMN pinned INTEGER DEFAULT 0");
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message implements Serializable, Model {
        // usar esto en ves del view model
        private long id;
        private String codMessage;
        private String recipientCode;
        private String senderCode;
        private String message;
        private TypeMessage type;
        private String createdDate;
        private String roomCode;
        private StatusMessage statusMessage;
        private boolean pinned;
        public static final class Column{
            public static final String ID= "id";
            public static final String COD_MESSAGE ="cod_message";
            public static final String RECIPIENT_CODE ="recipient_code";
            public static final String SENDER_CODE = "sender_code";
            public static final String MESSAGE = "message";
            public static final String TYPE = "type";
            public static final String CREATED_DATE = "created_date";
            public static final String ROOM_CODE = "room_code";
            public static final String STATUS_MESSAGE = "status_message";
            public static final String PINNED = "pinned";
        }

        @Override
        public void setId(long id) {
            this.id = id;
        }

        @Override
        public long getId() {
            return id;
        }
    }
}
