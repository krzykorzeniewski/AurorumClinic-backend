package pl.edu.pja.aurorumclinic.features.appointments.unregistered;


public interface AppointmentUnregisteredService {
    void createAppointmentForUnregisteredUser(CreateAppointmentUnregisteredRequest createRequest);
    void deleteAppointmentForUnregisteredUser(DeleteAppointmentUnregisteredRequest deleteRequest);
    void rescheduleAppointmentForUnregisteredUser(RescheduleAppointmentUnregisteredRequest rescheduleRequest);
}
