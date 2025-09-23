package pl.edu.pja.aurorumclinic.features.appointments.guests;


public interface AppointmentGuestService {
    void createAppointmentForUnregisteredUser(CreateAppointmentGuestRequest createRequest);
    void deleteAppointmentForUnregisteredUser(DeleteAppointmentGuestRequest deleteRequest);
    void rescheduleAppointmentForUnregisteredUser(RescheduleAppointmentGuestRequest rescheduleRequest);
}
