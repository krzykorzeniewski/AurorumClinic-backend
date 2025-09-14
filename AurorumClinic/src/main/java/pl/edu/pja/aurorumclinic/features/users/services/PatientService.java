package pl.edu.pja.aurorumclinic.features.users.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.users.dtos.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.PutPatientRequest;

import java.util.List;

@Service
public interface PatientService {

    List<GetPatientResponse> getAllPatients();
    GetPatientResponse getPatientById(Long patientId);
    GetPatientResponse partiallyUpdatePatient(Long patientId, PatchPatientRequest requestDto);
    GetPatientResponse updatePatient(Long patientId, PutPatientRequest requestDto);
    void deletePatient(Long id, Authentication authentication);
}
