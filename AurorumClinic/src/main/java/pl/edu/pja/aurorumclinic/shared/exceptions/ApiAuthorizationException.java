package pl.edu.pja.aurorumclinic.shared.exceptions;

import org.springframework.security.authorization.AuthorizationDeniedException;

public class ApiAuthorizationException extends AuthorizationDeniedException {
    public ApiAuthorizationException(String message) {
        super(message);
    }
}
