package pl.edu.pja.aurorumclinic.users.shared;

public class EmailVerificationTokenNotFoundException extends BadRequestException {
    public EmailVerificationTokenNotFoundException(String message) {
        super(message);
    }
}
