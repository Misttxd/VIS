package cz.gastro.service;

import cz.gastro.auth.CurrentUser;
import cz.gastro.dao.UserDao;
import cz.gastro.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserDao userDao;
    private final CurrentUser currentUser;

    public AuthService(UserDao userDao, CurrentUser currentUser) {
        this.userDao = userDao;
        this.currentUser = currentUser;
    }

    public User login(String username, String password) {
        User user = userDao.findByLogin(username, password);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nesprávné jméno nebo heslo.");
        }
        currentUser.setUser(user);
        return user;
    }

    public void logout() {
        currentUser.setUser(null);
    }
}
