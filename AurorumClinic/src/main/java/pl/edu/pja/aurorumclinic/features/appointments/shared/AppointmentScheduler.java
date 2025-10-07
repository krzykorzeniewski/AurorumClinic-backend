package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentFinishedEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateAppointmentsAndSendSurveys() {
        List<Appointment> appointmentsFromDb =
                appointmentRepository.getAllByFinishedAtBeforeAndStatusEquals(LocalDateTime.now(),
                        AppointmentStatus.CREATED);
        for (Appointment appointment : appointmentsFromDb) {
            appointment.setStatus(AppointmentStatus.FINISHED);
            applicationEventPublisher.publishEvent(new AppointmentFinishedEvent(appointment));
        }
    }

}
