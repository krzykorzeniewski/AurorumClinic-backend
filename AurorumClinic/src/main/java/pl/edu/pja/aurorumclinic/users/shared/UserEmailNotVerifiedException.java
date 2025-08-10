package pl.edu.pja.aurorumclinic.users.shared;

public class UserEmailNotVerifiedException extends BadRequestException {
    public UserEmailNotVerifiedException(String message) {
        super(message);
    }
}
