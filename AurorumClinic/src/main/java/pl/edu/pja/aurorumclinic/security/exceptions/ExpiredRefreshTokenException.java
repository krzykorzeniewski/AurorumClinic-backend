package pl.edu.pja.aurorumclinic.security.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ExpiredRefreshTokenException extends RuntimeException {
    public ExpiredRefreshTokenException(String msg) {
        super(msg);
    }
}
