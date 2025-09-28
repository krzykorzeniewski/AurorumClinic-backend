package pl.edu.pja.aurorumclinic.features.users.services;

import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientAppointmentsResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PutPatientRequest;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.util.List;

@Service
public interface PatientService {

    List<GetPatientResponse> getAllPatients(String query);
    GetPatientResponse getPatientById(Long patientId);
    GetPatientResponse partiallyUpdatePatient(Long patientId, PatchPatientRequest requestDto);
    GetPatientResponse updatePatient(Long patientId, PutPatientRequest requestDto);
    void deletePatient(Long id);
    GetPatientAppointmentsResponse getPatientAppointments(Long patientId);
}
