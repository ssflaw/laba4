package com.example.demo.dto;

/** Поля role/status (если переданы) не читаются: регистрация всегда создаёт активного STUDENT. */
public record RegisterRequest(String username, String email, String password, String deviceId) {
}
