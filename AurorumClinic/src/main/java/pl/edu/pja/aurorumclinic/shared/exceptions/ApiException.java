package pl.edu.pja.aurorumclinic.shared.exceptions;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException{

    private String field;

    public ApiException(String message, String field) {
        super(message);
        this.field = field;
    }
}
