package pl.edu.pja.aurorumclinic.features.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BadRequestException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
