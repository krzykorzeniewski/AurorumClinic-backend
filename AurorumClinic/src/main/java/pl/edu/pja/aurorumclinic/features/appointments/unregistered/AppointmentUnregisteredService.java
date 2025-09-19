package pl.edu.pja.aurorumclinic.features.appointments.unregistered;


public interface AppointmentUnregisteredService {
    void createAppointmentForUnregisteredUser(CreateAppointmentUnregisteredRequest createRequest);
    void deleteAppointmentForUnregisteredUser(String token);
    void rescheduleAppointmentForUnregisteredUser(String token, RescheduleAppointmentUnregisteredRequest rescheduleRequest);
}
