package edu.upb.chatupb_v2.Controller;

import edu.upb.chatupb_v2.Controller.exceptions.DatabaseException;
import edu.upb.chatupb_v2.Model.entities.Account;
import edu.upb.chatupb_v2.Model.repository.AccountDAO;

import java.util.List;
import java.util.UUID;

public class AccountController {

    private final AccountDAO accountDAO;

    public AccountController() {
        this.accountDAO = AccountDAO.getInstance();
        this.accountDAO.createTableIfNotExists();
    }

    public List<Account> getAccounts() {
        try {
            return accountDAO.findAll();
        } catch (Exception e) {
            throw new DatabaseException("No se pudieron cargar las cuentas: " + e.getMessage());
        }
    }

    public Account createAccount(String name) {
        Account newAccount = new Account(UUID.randomUUID().toString(), name);
        try {
            accountDAO.save(newAccount);
        } catch (Exception a){
            a.getMessage();
        }
        return newAccount;
    }
}