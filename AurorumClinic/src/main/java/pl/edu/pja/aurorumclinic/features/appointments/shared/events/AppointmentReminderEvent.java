package pl.edu.pja.aurorumclinic.features.appointments.shared.events;

import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;

public record AppointmentReminderEvent(Appointment appointment) {
}
