package pl.edu.pja.aurorumclinic.features.users.services;

import pl.edu.pja.aurorumclinic.features.users.dtos.request.*;

public interface UserService {

    void sendUpdateEmail(Long id, UpdateUserEmailTokenRequest requestDto);

    void updateUserPhoneNumber(Long id, UpdateUserPhoneNumberRequest requestDto);

    void sendUpdateSms(Long id, UpdateUserPhoneNumberTokenRequest requestDto);

    void updateUserEmail(Long id, UpdateUserEmailRequest requestDto);

    void send2faSms(Long id, UpdateUser2FATokenRequest requestDto);

    void updateUser2fa(Long id, UpdateUser2FARequest requestDto);
}
