package pl.edu.pja.aurorumclinic.users.shared;

public class EmailNotUniqueException extends BadRequestException {
    public EmailNotUniqueException(String message) {
        super(message);
    }
}
