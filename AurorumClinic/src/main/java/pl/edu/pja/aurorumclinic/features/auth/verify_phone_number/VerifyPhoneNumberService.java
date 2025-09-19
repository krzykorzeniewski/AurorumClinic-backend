package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number;

import org.springframework.security.core.Authentication;

public interface VerifyPhoneNumberService {
    void sendVerifyPhoneNumberSms(VerifyPhoneNumberTokenRequest verifyPhoneNumberTokenRequest, Authentication authentication);
    void verifyPhoneNumber(VerifyPhoneNumberRequest verifyPhoneNumberRequest, Authentication authentication);
}
