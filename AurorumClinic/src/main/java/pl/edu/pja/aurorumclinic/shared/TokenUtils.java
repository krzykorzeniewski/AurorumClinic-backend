package pl.edu.pja.aurorumclinic.shared;

import io.jsonwebtoken.io.Encoders;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TokenUtils {

    public String createRandomToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Encoders.BASE64.encode(bytes);
    }

    public String createOtp() {
        return String.format("%06d",new SecureRandom().nextInt(999999));
    }

}
