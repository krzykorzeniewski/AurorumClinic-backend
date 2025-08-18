package pl.edu.pja.aurorumclinic.shared;

public interface SmsService {

    void sendSms(String to, String from, String text);

}
