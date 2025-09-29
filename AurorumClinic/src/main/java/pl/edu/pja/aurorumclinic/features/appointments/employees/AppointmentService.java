package pl.edu.pja.aurorumclinic.features.appointments.employees;


public interface AppointmentService {

    void createAppointment(CreateAppointmentRequest request);
    void updateAppointment(Long appointmentId, UpdateAppointmentRequest request);

    void deleteAppointment(Long appointmentId);
}
