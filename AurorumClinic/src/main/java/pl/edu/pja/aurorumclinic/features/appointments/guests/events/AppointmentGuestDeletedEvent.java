package pl.edu.pja.aurorumclinic.features.appointments.guests.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Guest;

@Getter
@RequiredArgsConstructor
public class AppointmentGuestDeletedEvent {

    private final Guest guest;
    private final Appointment appointment;

}
