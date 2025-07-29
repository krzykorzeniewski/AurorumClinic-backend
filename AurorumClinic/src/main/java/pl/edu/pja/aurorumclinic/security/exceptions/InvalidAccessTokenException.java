package pl.edu.pja.aurorumclinic.security.exceptions;

import org.springframework.security.core.AuthenticationException;

public class InvalidAccessTokenException extends AuthenticationException {
    public InvalidAccessTokenException(String message) {
        super(message);
    }
}
