package pl.edu.pja.aurorumclinic.features.appointments.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentFinishedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentReminderEvent;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${workday.start.hour}")
    private Integer startOfDay;

    @Value("${workday.end.hour}")
    private Integer endOfDay;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateAppointmentsStatus() {
        List<Appointment> appointmentsFromDb =
                appointmentRepository.getAllByFinishedAtBeforeAndStatusEquals(LocalDateTime.now(),
                        AppointmentStatus.CREATED);
        if (appointmentsFromDb.isEmpty()) {
            return;
        }
        for (Appointment appointment : appointmentsFromDb) {
            appointment.setStatus(AppointmentStatus.FINISHED);
            applicationEventPublisher.publishEvent(new AppointmentFinishedEvent(appointment));
        }
    }

    @Scheduled(cron = "${workday.notifications-scheduler}")
    @Transactional
    public void appointmentReminderNotification() {
        LocalDateTime startOfTodayDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(startOfDay, 0));
        LocalDateTime endOfTodayDate = LocalDateTime.of(LocalDate.now(), LocalTime.of(endOfDay, 0));
        LocalDateTime startOfTomorrowDate = startOfTodayDate.plusDays(1);
        LocalDateTime endOfTomorrowDate = endOfTodayDate.plusDays(1);
        List<Appointment> appointmentsFromDb =
                appointmentRepository.getAllByStartedAtBetweenAndNotificationSentEquals(startOfTomorrowDate,
                        endOfTomorrowDate, false);
        if (appointmentsFromDb.isEmpty()) {
            return;
        }
        for (Appointment appointment : appointmentsFromDb) {
            appointment.setNotificationSent(true);
            applicationEventPublisher.publishEvent(new AppointmentReminderEvent(appointment));
        }
    }

}
