package pl.edu.pja.aurorumclinic.shared;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException{

    private String field;

    public ApiException(String message, String field) {
        super(message);
        this.field = field;
    }
}
