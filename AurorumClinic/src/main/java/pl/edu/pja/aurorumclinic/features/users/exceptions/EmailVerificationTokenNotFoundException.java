package pl.edu.pja.aurorumclinic.features.users.exceptions;

public class EmailVerificationTokenNotFoundException extends BadRequestException {
    public EmailVerificationTokenNotFoundException(String message) {
        super(message);
    }
}
