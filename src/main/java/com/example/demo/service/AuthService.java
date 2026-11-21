package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.TokenPair;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.Check;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int MAX_DEVICE_ID = 255;
    private static final String BAD_CREDENTIALS = "invalid username or password";

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository users;
    private final TokenService tokens;

    public AuthService(AuthenticationManager authenticationManager, UserService userService,
                       UserRepository users, TokenService tokens) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.users = users;
        this.tokens = tokens;
    }

    @Transactional
    public TokenPair login(LoginRequest req) {
        Check.require(req != null && Check.notBlank(req.username()) && Check.notBlank(req.password()),
                "username and password are required");
        String deviceId = deviceId(req.deviceId());
        try {
            authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password()));
        } catch (AuthenticationException e) {
            // Одинаковый ответ для неверного пароля, несуществующего и заблокированного пользователя.
            throw new UnauthorizedException(BAD_CREDENTIALS);
        }
        User user = users.findByUsername(req.username())
                .orElseThrow(() -> new UnauthorizedException(BAD_CREDENTIALS));
        return tokens.issue(user, deviceId);
    }

    /** Пользователь и его первая сессия создаются в одной транзакции: ошибка записи сессии откатывает регистрацию. */
    @Transactional
    public TokenPair register(RegisterRequest req) {
        Check.require(req != null, "request body is required");
        String deviceId = deviceId(req.deviceId());
        User u = new User();
        u.username = req.username();
        u.email = req.email();
        u.password = req.password();
        User saved = userService.register(u); // проверки данных, уникальности, роль всегда STUDENT
        return tokens.issue(saved, deviceId);
    }

    /**
     * Без @Transactional: транзакция открывается в TokenService.refresh, иначе внешняя транзакция
     * откатила бы перевод сессии в REVOKED при отказе.
     */
    public TokenPair refresh(RefreshRequest req) {
        Check.require(req != null && Check.notBlank(req.refreshToken()), "refreshToken is required");
        return tokens.refresh(req.refreshToken());
    }

    private static String deviceId(String raw) {
        if (!Check.notBlank(raw)) return null;
        String v = raw.trim();
        Check.require(v.length() <= MAX_DEVICE_ID, "deviceId must be at most " + MAX_DEVICE_ID + " characters");
        return v;
    }
}
