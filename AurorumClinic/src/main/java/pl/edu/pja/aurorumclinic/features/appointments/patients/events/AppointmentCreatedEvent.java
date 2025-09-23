package pl.edu.pja.aurorumclinic.features.appointments.patients.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

@RequiredArgsConstructor
@Getter
public class AppointmentCreatedEvent {

    private final Patient patient;
    private final Appointment appointment;
    private final String rescheduleLink;
    private final String deleteLink;
}
