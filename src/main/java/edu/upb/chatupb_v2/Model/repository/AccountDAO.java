package edu.upb.chatupb_v2.Model.repository;


import edu.upb.chatupb_v2.Model.entities.Account;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AccountDAO {

    private final DaoHelper<Account> helper;
    private static final AccountDAO instance = new AccountDAO();

    public static AccountDAO getInstance() {
        return instance;
    }

    private AccountDAO() {
        helper = new DaoHelper<>();
    }

    DaoHelper.ResultReader<Account> resultReader = result -> {
        Account account = new Account();
        if (AccountDAO.existColumn(result, Account.Column.ID_ACCOUNT)) {
            account.setId(result.getString("id_account"));
        }
        if (AccountDAO.existColumn(result, Account.Column.NOMBRE)) {
            account.setNombre(result.getString("nombre"));
        }
        return account;
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

    /**
     * Returns all accounts from the accounts table.
     */
    public List<Account> findAll() throws ConnectException, SQLException {
        String query = "SELECT id_account, nombre FROM accounts";
        return helper.executeQuery(query, resultReader);
    }

    /**
     * Saves a new account (only id and name — no IP).
     */
    public void save(Account account) throws Exception {
        String query = "INSERT INTO accounts(id_account, nombre) VALUES (?, ?)";
        DaoHelper.QueryParameters params = new DaoHelper.QueryParameters() {
            @Override
            public void setParameters(PreparedStatement pst) throws SQLException {
                pst.setString(1, account.getId());
                pst.setString(2, account.getNombre());
            }
        };
        helper.insert(query, params, account);
    }

    /**
     * Creates the accounts table if it doesn't exist yet.
     */
    public void createTableIfNotExists() {
        try {
            String ddl = "CREATE TABLE IF NOT EXISTS accounts (id TEXT PRIMARY KEY, name TEXT NOT NULL)";
            helper.update(ddl, null);
        } catch (Exception e) {
            System.err.println("Error creating accounts table: " + e.getMessage());
        }
    }
}