package cz.gastro.dao;

import cz.gastro.model.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
    private final JdbcClient db;

    public UserDao(JdbcClient db) {
        this.db = db;
    }

    public User findByLogin(String username, String password) {
        return db.sql("""
                SELECT id, username, role
                FROM app_user
                WHERE username = ? AND password = ?
                """)
                .param(username)
                .param(password)
                .query(User.class)
                .optional()
                .orElse(null);
    }
}
