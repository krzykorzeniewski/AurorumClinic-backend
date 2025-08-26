package pl.edu.pja.aurorumclinic.shared.exceptions;

public class ApiNotFoundException extends ApiException{
    public ApiNotFoundException(String message, String field) {
        super(message, field);
    }
}
