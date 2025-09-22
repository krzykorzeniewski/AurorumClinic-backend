package pl.edu.pja.aurorumclinic.features.appointments.registered;


import org.springframework.security.core.Authentication;

public interface AppointmentService {
    void createAppointment(CreateAppointmentRequest createAppointmentRequest, Long userId);
}
