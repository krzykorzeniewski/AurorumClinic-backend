package pl.edu.pja.aurorumclinic.features.appointments.services;

import jakarta.validation.Valid;
import pl.edu.pja.aurorumclinic.features.appointments.dtos.CreateAppointmentUnregisteredRequest;

public interface AppointmentService {
    void createAppointmentForUnregisteredUser(@Valid CreateAppointmentUnregisteredRequest createRequest);
    void deleteAppointmentForUnregisteredUser(String token);
}
