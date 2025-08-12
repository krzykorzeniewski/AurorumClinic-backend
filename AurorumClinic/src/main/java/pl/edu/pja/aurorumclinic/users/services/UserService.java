package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Doctor;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.models.User;
import pl.edu.pja.aurorumclinic.users.dtos.*;

@Service
public interface UserService {

    User registerEmployee(RegisterEmployeeRequestDto requestDto);
    Patient registerPatient(RegisterPatientRequestDto requestDto);
    Doctor registerDoctor(RegisterDoctorRequestDto requestDto);
    AccessTokenResponseDto loginUser(LoginUserRequestDto requestDto);
    AccessTokenResponseDto refreshAccessToken(RefreshTokenRequestDto requestDto);
    void verifyUserEmail(String token);
    void sendResetPasswordEmail(ForgetPasswordRequestDto requestDto);
    void resetPassword(ResetPasswordRequestDto requestDto);
}
