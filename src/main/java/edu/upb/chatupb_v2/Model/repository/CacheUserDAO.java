package edu.upb.chatupb_v2.Model.repository;

import edu.upb.chatupb_v2.Model.entities.User;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheUserDAO implements IUserDao{
    private final Map<String, User> users = new HashMap<>();
    private final IUserDao iUserDao;
    public CacheUserDAO(IUserDao iUserDao){
        super();
        this.iUserDao = iUserDao;
    }
    @Override
    public List<User> findAll() throws ConnectException, SQLException {
        if (users.isEmpty()){
            List<User> users1 = iUserDao.findAll();
            for (User user : users1) {
                users.put(user.getId(), user);
            }
        }
        return (List<User>) users.values();
    }
    @Override
    public List<User> findAllContactsForUser(String id) throws ConnectException, SQLException {
        List<User> s = users.values().stream().filter(x -> x.getSavedById().equals(id)).toList();
        if (s.isEmpty()){
            s = iUserDao.findAllContactsForUser(id);
        }
        return s;
    }
    @Override
    public boolean exist(String argument) throws ConnectException, SQLException {
        return false;
    }
    @Override
    public boolean existByCode(String id) throws ConnectException, SQLException {
        return false;
    }
    @Override
    public User findById(String id) throws ConnectException, SQLException {
        return users.get(id) == null ? findById(id) : users.get(id);
    }
    @Override
    public void deleteUser(String id) throws ConnectException, SQLException {
        iUserDao.deleteUser(id);
        users.remove(id);
    }
    @Override
    public void update(String query) throws Exception {

    }
    @Override
    public void save(User user) throws Exception {
        iUserDao.save(user);
        users.put(user.getId(), user);
    }
    @Override
    public void update(String query, String conditionWhere) throws SQLException, ConnectException {

    }
}
