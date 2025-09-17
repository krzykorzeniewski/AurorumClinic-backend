package pl.edu.pja.aurorumclinic.features.users.services;

import jakarta.validation.Valid;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserEmailTokenRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserPhoneNumberRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.UpdateUserPhoneNumberTokenRequest;

public interface UserService {

    void sendUpdateEmail(Long id, UpdateUserEmailTokenRequest requestDto);

    void updateUserPhoneNumber(Long id, UpdateUserPhoneNumberRequest requestDto);

    void sendUpdateSms(Long id, UpdateUserPhoneNumberTokenRequest requestDto);

    void updateUserEmail(Long id, UpdateUserEmailRequest requestDto);
}
