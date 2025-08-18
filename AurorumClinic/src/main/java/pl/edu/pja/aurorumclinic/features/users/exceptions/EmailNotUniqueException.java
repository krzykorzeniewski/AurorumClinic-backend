package pl.edu.pja.aurorumclinic.features.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailNotUniqueException extends BadRequestException {
    public EmailNotUniqueException(String message) {
        super(message);
    }
}
