package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.users.dtos.*;

@Service
public interface UserService {

    void registerEmployee(RegisterEmployeeRequestDto requestDto) throws Exception;
    void registerPatient(RegisterPatientRequestDto requestDto) throws Exception;
    AccessTokenResponseDto loginUser(LoginUserRequestDto requestDto);
    AccessTokenResponseDto refreshAccessToken(RefreshTokenRequestDto requestDto);

}
