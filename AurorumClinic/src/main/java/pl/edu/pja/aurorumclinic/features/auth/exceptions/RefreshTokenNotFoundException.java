package pl.edu.pja.aurorumclinic.features.auth.exceptions;

import org.springframework.security.core.AuthenticationException;

public class RefreshTokenNotFoundException extends AuthenticationException {
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }
}
