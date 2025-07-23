package pl.edu.pja.aurorumclinic.security.exceptions;

import org.springframework.security.core.AuthenticationException;

public class ExpiredRefreshTokenException extends AuthenticationException {
    public ExpiredRefreshTokenException(String msg) {
        super(msg);
    }
}
