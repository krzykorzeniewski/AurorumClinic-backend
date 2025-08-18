package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Doctor;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.models.User;
import pl.edu.pja.aurorumclinic.users.dtos.request.*;
import pl.edu.pja.aurorumclinic.users.dtos.response.LoginUserResponse;
import pl.edu.pja.aurorumclinic.users.dtos.response.RefreshAccessTokenResponse;
import pl.edu.pja.aurorumclinic.users.dtos.response.TwoFactorAuthLoginResponse;

@Service
public interface UserService {

    User registerEmployee(RegisterEmployeeRequest requestDto);
    Patient registerPatient(RegisterPatientRequest requestDto);
    Doctor registerDoctor(RegisterDoctorRequest requestDto);
    LoginUserResponse loginUser(LoginUserRequest requestDto);
    RefreshAccessTokenResponse refreshAccessToken(RefreshAccessTokenRequest requestDto);
    void verifyUserEmail(String token);
    void sendResetPasswordEmail(PasswordResetTokenRequest requestDto);
    void resetPassword(ResetPasswordRequest requestDto);
    TwoFactorAuthLoginResponse loginUserWithTwoFactorAuth(TwoFactorAuthLoginRequest requestDto);
    void sendVerifyUserAccountEmail(VerifyEmailTokenRequest verifyEmailTokenRequest);
    void send2faToken(TwoFactorAuthTokenRequest twoFactorAuthTokenRequest);
}
