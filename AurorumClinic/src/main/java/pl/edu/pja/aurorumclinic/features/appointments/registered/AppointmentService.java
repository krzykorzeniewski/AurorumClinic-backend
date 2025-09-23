package pl.edu.pja.aurorumclinic.features.appointments.registered;


import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.DeleteAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.registered.dtos.response.GetAppointmentPatientResponse;

public interface AppointmentService {
    void createAppointment(CreateAppointmentPatientRequest createAppointmentPatientRequest, Long userId);

    void updateAppointment(UpdateAppointmentPatientRequest updateAppointmentPatientRequest, Long userId);

    void deleteAppointment(DeleteAppointmentPatientRequest deleteAppointmentPatientRequest, Long userId);

    GetAppointmentPatientResponse getAppointmentForPatient(Long appointmentId, Long userId);
}
