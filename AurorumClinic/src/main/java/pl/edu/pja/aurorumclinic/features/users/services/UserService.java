package pl.edu.pja.aurorumclinic.features.users.services;

import org.springframework.security.core.Authentication;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserPhoneNumberRequest;

public interface UserService {

    void updateUserEmail(Long id, UpdateUserEmailRequest requestDto, Authentication authentication);

    void updateUserPhoneNumber(Long id, UpdateUserPhoneNumberRequest requestDto, Authentication authentication);

}
