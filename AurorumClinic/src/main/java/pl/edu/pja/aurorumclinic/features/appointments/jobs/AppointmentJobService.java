package pl.edu.pja.aurorumclinic.features.appointments.jobs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentJobService {

    private final TaskScheduler taskScheduler;
    private final FinishAppointmentJob finishAppointmentJob;
    private final AppointmentNotificationJob appointmentNotificationJob;
    private final AppointmentRepository appointmentRepository;

    @Value("${workday.start.hour}")
    private Integer startOfDay;

    @Value("${workday.end.hour}")
    private Integer endOfDay;

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
        taskScheduler.schedule(() -> appointmentNotificationJob.execute(event.getAppointment().getId()),
                event.getAppointment().getStartedAt().minusHours(24)
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
    }

//    @EventListener
//    public void onApplicationStart(ContextRefreshedEvent event) {
//        scheduleAllUnfinished();
//        scheduleAllWithoutNotification();
//    }

    private void scheduleAllUnfinished() {
        List<Appointment> notFinishedAppointments =
                appointmentRepository.getAllByFinishedAtBeforeAndStatusEquals(LocalDateTime.now(),
                        AppointmentStatus.CREATED);
        if (!notFinishedAppointments.isEmpty()) {
            for (Appointment appointment : notFinishedAppointments) {
                taskScheduler.schedule(() -> finishAppointmentJob.execute(appointment.getId()),
                        appointment.getFinishedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
            }
        }
    }

    private void scheduleAllWithoutNotification() {
        LocalDateTime startOfTodayDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(startOfDay, 0));
        LocalDateTime endOfTodayDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(endOfDay, 0));
        LocalDateTime startOfTomorrowDate = startOfTodayDate.plusDays(1);
        LocalDateTime endOfTomorrowDate = endOfTodayDate.plusDays(1);
        List<Appointment> appointmentsWithoutNotification =
                appointmentRepository.getAllByStartedAtBetweenAndNotificationSentEquals(startOfTomorrowDate,
                        endOfTomorrowDate, false);

        if (!appointmentsWithoutNotification.isEmpty()) {
            for (Appointment appointment : appointmentsWithoutNotification) {
                taskScheduler.schedule(() -> appointmentNotificationJob.execute(appointment.getId()),
                        appointment.getStartedAt().minusHours(24)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
            }
        }
    }

}
