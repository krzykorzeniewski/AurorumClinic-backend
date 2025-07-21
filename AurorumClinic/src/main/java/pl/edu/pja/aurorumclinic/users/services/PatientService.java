package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.users.dtos.LoginPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.RefreshTokenRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.RegisterPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.TokenResponseDto;

@Service
public interface PatientService {

    void registerPatient(RegisterPatientRequestDto requestDto) throws Exception;

    TokenResponseDto loginPatient(LoginPatientRequestDto requestDto);

    TokenResponseDto refreshAccessToken(RefreshTokenRequestDto requestDto);
}
