package cz.rest.controller;

import cz.rest.auth.CurrentUser;
import cz.rest.dto.LoginRequest;
import cz.rest.model.User;
import cz.rest.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        return authService.login(request.username(), request.password());
    }

    @PostMapping("/logout")
    public void logout() {
        authService.logout();
    }

    @GetMapping("/current")
    public User current() {
        return currentUser.getUser();
    }
}