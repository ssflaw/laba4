package com.example.demo.controller;

import com.example.demo.dto.RefreshRequest;
import com.example.demo.dto.SessionView;
import com.example.demo.repository.UserSessionRepository;
import com.example.demo.util.Check;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Только для TEACHER: состояние сессии по refresh-токену (сами токены не возвращаются). */
@RestController
@PreAuthorize("hasRole('TEACHER')")
@RequestMapping("/api/sessions")
public class SessionController {

    private final UserSessionRepository sessions;

    public SessionController(UserSessionRepository sessions) {
        this.sessions = sessions;
    }

    @PostMapping("/lookup")
    public SessionView lookup(@RequestBody RefreshRequest body) {
        Check.require(body != null && Check.notBlank(body.refreshToken()), "refreshToken is required");
        return SessionView.of(Check.found(sessions.findByRefreshToken(body.refreshToken()), "Session"));
    }
}
