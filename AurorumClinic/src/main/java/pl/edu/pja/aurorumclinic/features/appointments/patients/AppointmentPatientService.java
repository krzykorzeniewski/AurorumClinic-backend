package pl.edu.pja.aurorumclinic.features.appointments.patients;


import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.DeleteAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.response.GetAppointmentPatientResponse;

public interface AppointmentPatientService {
    void createAppointment(CreateAppointmentPatientRequest createAppointmentPatientRequest, Long userId);

    void updateAppointment(UpdateAppointmentPatientRequest updateAppointmentPatientRequest, Long userId);

    void deleteAppointment(DeleteAppointmentPatientRequest deleteAppointmentPatientRequest, Long userId);

    GetAppointmentPatientResponse getAppointmentForPatient(Long appointmentId, Long userId);
}
