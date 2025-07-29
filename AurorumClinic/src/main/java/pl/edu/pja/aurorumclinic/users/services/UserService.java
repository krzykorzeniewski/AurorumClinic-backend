package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.models.User;
import pl.edu.pja.aurorumclinic.users.dtos.*;

@Service
public interface UserService {

    User registerEmployee(RegisterEmployeeRequestDto requestDto);
    Patient registerPatient(RegisterPatientRequestDto requestDto);
    AccessTokenDto loginUser(LoginUserRequestDto requestDto);
    AccessTokenDto refreshAccessToken(RefreshTokenRequestDto requestDto);

}
