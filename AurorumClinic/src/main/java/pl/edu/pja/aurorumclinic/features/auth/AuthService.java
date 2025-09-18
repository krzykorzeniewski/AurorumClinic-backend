package pl.edu.pja.aurorumclinic.features.auth;

import org.springframework.security.core.Authentication;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.features.auth.dtos.request.*;
import pl.edu.pja.aurorumclinic.features.auth.dtos.response.*;
public interface AuthService {

    User registerEmployee(RegisterEmployeeRequest requestDto);
    Patient registerPatient(RegisterPatientRequest requestDto);
    Doctor registerDoctor(RegisterDoctorRequest requestDto);
    LoginUserResponse loginUser(LoginUserRequest requestDto);
    RefreshAccessTokenResponse refreshAccessToken(RefreshAccessTokenRequest requestDto);
    void verifyUserEmail(VerifyEmailRequest requestDto);
    void sendResetPasswordEmail(PasswordResetTokenRequest requestDto);
    void resetPassword(ResetPasswordRequest requestDto);
    TwoFactorAuthLoginResponse loginUserWithTwoFactorAuth(TwoFactorAuthLoginRequest requestDto);
    void sendVerifyUserAccountEmail(VerifyEmailTokenRequest verifyEmailTokenRequest);
    void send2faToken(TwoFactorAuthTokenRequest twoFactorAuthTokenRequest);
    void sendVerifyPhoneNumberMessage(VerifyPhoneNumberTokenRequest requestDto, Authentication authentication);
    void verifyPhoneNumber(VerifyPhoneNumberRequest requestDto, Authentication authentication);
}
