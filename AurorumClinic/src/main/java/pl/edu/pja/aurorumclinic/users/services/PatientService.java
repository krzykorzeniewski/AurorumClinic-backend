package pl.edu.pja.aurorumclinic.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.users.dtos.response.GetPatientResponse;
import pl.edu.pja.aurorumclinic.users.dtos.request.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.users.dtos.request.PutPatientRequest;

import java.util.List;

@Service
public interface PatientService {

    List<GetPatientResponse> getAllPatients();
    GetPatientResponse getPatientById(Long patientId);
    GetPatientResponse partiallyUpdatePatient(Long patientId, PatchPatientRequest requestDto);
    GetPatientResponse updatePatient(Long patientId, PutPatientRequest requestDto);

}
