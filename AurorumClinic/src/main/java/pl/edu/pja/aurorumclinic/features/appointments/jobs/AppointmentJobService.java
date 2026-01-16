package pl.edu.pja.aurorumclinic.features.appointments.jobs;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentJobService {

    private final TaskScheduler taskScheduler;
    private final FinishAppointmentJob finishAppointmentJob;
    private final AppointmentNotificationJob appointmentNotificationJob;
    private final AppointmentRepository appointmentRepository;

    @Order(1)
    @TransactionalEventListener
    public void onAppointmentCreatedEventStatus(AppointmentCreatedEvent event) {
        taskScheduler.schedule(() -> finishAppointmentJob.execute(event.getAppointment().getId()),
                event.getAppointment().getStartedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
    }

    @Order(2)
    @TransactionalEventListener
    public void onAppointmentCreatedEventNotification(AppointmentCreatedEvent event) {
        if (event.getAppointment().getStartedAt().minusHours(24).isBefore(LocalDateTime.now())) {
            return;
        }
        taskScheduler.schedule(() -> appointmentNotificationJob.execute(event.getAppointment().getId()),
                event.getAppointment().getStartedAt().minusHours(24)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void finishAppointments() {
        List<Tuple> notFinishedAppointments = appointmentRepository
                .getAllByStatusAndFinishedAt(AppointmentStatus.CREATED, LocalDateTime.now());
        if (!notFinishedAppointments.isEmpty()) {
            for (Tuple appointment : notFinishedAppointments) {
                taskScheduler.schedule(() -> finishAppointmentJob.execute(appointment.get("id", Long.class)),
                        appointment.get("startedAt", LocalDateTime.class)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
            }
        }
    }

    @EventListener
    public void onApplicationStart(ContextRefreshedEvent event) {
        scheduleAllUnfinished();
        scheduleAllWithoutNotification();
    }

    private void scheduleAllUnfinished() {
        List<Tuple> notFinishedAppointments = appointmentRepository.getAllByStatus(AppointmentStatus.CREATED);
        if (!notFinishedAppointments.isEmpty()) {
            for (Tuple appointment : notFinishedAppointments) {
                taskScheduler.schedule(() -> finishAppointmentJob.execute(appointment.get("id", Long.class)),
                        appointment.get("startedAt", LocalDateTime.class)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
            }
        }
    }

    private void scheduleAllWithoutNotification() {
        List<Tuple> appointmentsWithoutNotification =
                appointmentRepository.getAllByNotificationSent(false);

        if (!appointmentsWithoutNotification.isEmpty()) {
            for (Tuple appointment : appointmentsWithoutNotification) {
                if (!appointment.get("startedAt", LocalDateTime.class).minusHours(24).isBefore(LocalDateTime.now())) {
                    taskScheduler.schedule(() -> appointmentNotificationJob.execute(appointment.get("id", Long.class)),
                            appointment.get("startedAt", LocalDateTime.class).minusHours(24)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant());
                }
            }
        }
    }
}
