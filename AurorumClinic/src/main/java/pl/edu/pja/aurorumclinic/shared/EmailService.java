package pl.edu.pja.aurorumclinic.shared;

public interface EmailService {

    void sendEmail(String from, String to, String subject, String text);

}
