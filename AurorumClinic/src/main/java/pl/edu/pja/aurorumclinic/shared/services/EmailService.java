package pl.edu.pja.aurorumclinic.shared.services;

public interface EmailService {

    void sendEmail(String from, String to, String subject, String text);

}
