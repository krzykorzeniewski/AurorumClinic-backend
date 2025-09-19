package pl.edu.pja.aurorumclinic.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ApiConflictException extends ApiException {

    public ApiConflictException(String message, String field) {
        super(message, field);
    }
}
