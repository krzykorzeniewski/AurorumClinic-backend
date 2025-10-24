package pl.edu.pja.aurorumclinic.features.appointments.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AppointmentJobService {

    private final TaskScheduler taskScheduler;
    private final FinishAppointmentJob finishAppointmentJob;
    private final NotificationAppointmentJob notificationAppointmentJob;

    @Order(1)
    @TransactionalEventListener
    public void onAppointmentCreatedEventStatus(AppointmentCreatedEvent event) {
        taskScheduler.schedule(() -> finishAppointmentJob.execute(event.getAppointment().getId()),
                event.getAppointment().getFinishedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
    }

    @Order(2)
    @TransactionalEventListener
    public void onAppointmentCreatedEventNotification(AppointmentCreatedEvent event) {
        if (event.getAppointment().getStartedAt().minusHours(24).isBefore(LocalDateTime.now())) {
            return;
        }
        taskScheduler.schedule(() -> notificationAppointmentJob.execute(event.getAppointment().getId()),
                event.getAppointment().getStartedAt().minusHours(24)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
    }

    @EventListener
    public void onApplicationStart(ContextRefreshedEvent event) {
        //TODO utilize sent_at columns
    }

}
