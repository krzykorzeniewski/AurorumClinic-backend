package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.users.dtos.GetPatientResponseDto;
import pl.edu.pja.aurorumclinic.users.dtos.PatchPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.PutPatientRequestDto;

import java.util.List;

@Service
public interface PatientService {

    List<GetPatientResponseDto> getAllPatients();
    GetPatientResponseDto getPatientById(Long patientId);
    GetPatientResponseDto partiallyUpdatePatient(Long patientId, PatchPatientRequestDto requestDto);
    GetPatientResponseDto updatePatient(Long patientId, PutPatientRequestDto requestDto);

}
