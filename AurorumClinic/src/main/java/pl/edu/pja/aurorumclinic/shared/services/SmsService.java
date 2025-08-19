package pl.edu.pja.aurorumclinic.shared.services;

public interface SmsService {

    void sendSms(String to, String from, String text);

}
