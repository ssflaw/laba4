package com.example.demo.exception;

/**
 * Повторно предъявлен уже использованный refresh. Сессия переводится в REVOKED,
 * и это изменение фиксируется, хотя клиент получает 401 (см. noRollbackFor в TokenService).
 */
public class RefreshTokenReuseException extends UnauthorizedException {
    public RefreshTokenReuseException() {
        super("refresh token has already been used, session revoked");
    }
}
