package pl.edu.pja.aurorumclinic.features.users.exceptions;

public class UserEmailNotVerifiedException extends BadRequestException {
    public UserEmailNotVerifiedException(String message) {
        super(message);
    }
}
