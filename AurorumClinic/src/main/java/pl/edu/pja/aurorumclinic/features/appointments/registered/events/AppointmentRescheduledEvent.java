package pl.edu.pja.aurorumclinic.features.appointments.registered.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

@RequiredArgsConstructor
@Getter
public class AppointmentRescheduledEvent {

    private final Patient patient;
    private final String rescheduleLink;
    private final String deleteLink;
    private final Appointment appointment;

}
