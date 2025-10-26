package pl.edu.pja.aurorumclinic.features.appointments.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentReminderEvent;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationJob {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void execute(Long appointmentId) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        appointmentFromDb.setNotificationSent(true);
        applicationEventPublisher.publishEvent(new AppointmentReminderEvent(appointmentFromDb));
    }

}
