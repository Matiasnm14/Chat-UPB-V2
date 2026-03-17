package edu.upb.chatupb_v2.model.repository;

import edu.upb.chatupb_v2.model.entities.comands.AcceptHello;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class ContactDao implements IContactDAO {


    private DaoHelper<AcceptHello.User.Contact> helper;

    public ContactDao() {
        helper = new DaoHelper<>();
        ensureTable();
        ensureThemeColumn();
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
        if (existColumn(result, AcceptHello.User.Contact.Column.THEME_ID)) {
            prefacturaSync.setThemeId(result.getString(AcceptHello.User.Contact.Column.THEME_ID));
        }
        return prefacturaSync;
    };

    private void ensureTable() {
        String query = "CREATE TABLE IF NOT EXISTS contact (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT UNIQUE NOT NULL," +
                "name TEXT," +
                "ip TEXT," +
                "theme_id TEXT DEFAULT 'default'" +
                ")";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.execute();
        } catch (Exception ignored) {
        }
    }

    private void ensureThemeColumn() {
        String query = "ALTER TABLE contact ADD COLUMN theme_id TEXT DEFAULT 'default'";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.execute();
        } catch (Exception ignored) {
        }
    }

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

    public boolean existByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM contact WHERE code='" + code + "'";
        return helper.executeQueryCount(query, null) >= 1;
    }

    public boolean existByIp(String ip) throws ConnectException, SQLException {
        String query = "SELECT count(*) FROM contact WHERE ip='" + ip + "'";
        return helper.executeQueryCount(query, null) >= 1;
    }

    public AcceptHello.User.Contact findByIp(String ip) throws ConnectException, SQLException {
        String query = "SELECT * FROM contact WHERE ip ='" + ip + "'";
        List<AcceptHello.User.Contact> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public AcceptHello.User.Contact findByCode(String code) throws ConnectException, SQLException {
        String query = "SELECT * FROM contact WHERE code ='" + code + "'";
        List<AcceptHello.User.Contact> list = helper.executeQuery(query, resultReader);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public void updateIpByCode(String code, String ip) throws Exception {
        String query = "UPDATE contact SET ip='" + ip + "' WHERE code='" + code + "'";
        helper.update(query, null);
    }

    public void updateThemeByCode(String code, String themeId) throws Exception {
        String query = "UPDATE contact SET theme_id = ? WHERE code = ?";
        DaoHelper.QueryParameters params = pst -> {
            pst.setString(1, themeId == null || themeId.isBlank() ? "default" : themeId.trim());
            pst.setString(2, code);
        };
        helper.update(query, params);
    }

    public void update(String query) throws Exception {
        helper.update(query, null);
    }

    public void save(AcceptHello.User.Contact contact) throws Exception {
        String query = "INSERT INTO contact(code, name, ip, theme_id) values (?,?,?,?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, contact.getCode());
                pst.setString(2, contact.getName());
                pst.setString(3, contact.getIp());
                pst.setString(4, contact.getThemeId() == null || contact.getThemeId().isBlank() ? "default" : contact.getThemeId().trim());
            }
        };
        helper.insert(query, params, contact);
    }

    public void deleteByCode(String code) throws Exception {
        String query = "DELETE FROM contact WHERE code = ?";
        DaoHelper.QueryParameters params = pst -> pst.setString(1, code);
        helper.update(query, params);
    }

    public void deleteAll() throws Exception {
        String query = "DELETE FROM contact";
        helper.update(query, null);
    }
}
