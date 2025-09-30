package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;


public interface VerifyPhoneNumberService {
    void sendVerifyPhoneNumberSms(Long userId);
    void verifyPhoneNumber(VerifyPhoneNumberRequest verifyPhoneNumberRequest, Long userId);
}
