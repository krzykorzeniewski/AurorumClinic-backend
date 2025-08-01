package pl.edu.pja.aurorumclinic.users.shared;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BadRequestException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
