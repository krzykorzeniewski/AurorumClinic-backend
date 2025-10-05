package pl.edu.pja.aurorumclinic.features.users.services;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientAppointmentResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PutPatientRequest;

import java.util.List;

@Service
public interface PatientService {

    List<GetPatientResponse> getAllPatients(String query, int page, int size);
    GetPatientResponse getPatientById(Long patientId);
    GetPatientResponse partiallyUpdatePatient(Long patientId, PatchPatientRequest requestDto);
    GetPatientResponse updatePatient(Long patientId, PutPatientRequest requestDto);
    void deletePatient(Long id);
    Page<GetPatientAppointmentResponse> getPatientAppointments(Long patientId, int page, int size);
}
