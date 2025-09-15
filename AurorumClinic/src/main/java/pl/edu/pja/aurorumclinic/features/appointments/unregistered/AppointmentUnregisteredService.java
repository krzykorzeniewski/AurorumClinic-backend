package pl.edu.pja.aurorumclinic.features.appointments.unregistered;

import jakarta.validation.Valid;

public interface AppointmentUnregisteredService {
    void createAppointmentForUnregisteredUser(@Valid CreateAppointmentUnregisteredRequest createRequest);
    void deleteAppointmentForUnregisteredUser(String token);
}
