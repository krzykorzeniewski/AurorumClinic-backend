package pl.edu.pja.aurorumclinic.features.auth.shared;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class ApiAuthException extends AuthenticationException {
    private String field;

    public ApiAuthException(String message, String field) {
        super(message);
        this.field = field;
    }
}
