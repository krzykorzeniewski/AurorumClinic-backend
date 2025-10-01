package pl.edu.pja.aurorumclinic.features.appointments.patients;

import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.response.GetAppointmentPatientResponse;

import java.io.IOException;

public interface AppointmentPatientService {
    void createAppointment(CreateAppointmentPatientRequest createAppointmentPatientRequest, Long userId);

    void updateAppointment(UpdateAppointmentPatientRequest updateAppointmentPatientRequest, Long userId, Long appointmentId);

    void deleteAppointment(Long appointmentId, Long userId);

    GetAppointmentPatientResponse getAppointmentForPatient(Long appointmentId, Long userId) throws IOException;
}
