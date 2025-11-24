package pl.edu.pja.aurorumclinic.shared;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PasswordValidator {

    public void validatePassword(String password) {
        Pattern pattern = Pattern.compile("^((?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])).{10,}$");
        Matcher matcher = pattern.matcher(password);
        if (!matcher.find()) {
            throw new ApiException("Password has to be minimum 10 characters long, " +
                    "have at least 1 uppercase, 1 lowercase character and 1 digit", "password");
        }
    }

}
