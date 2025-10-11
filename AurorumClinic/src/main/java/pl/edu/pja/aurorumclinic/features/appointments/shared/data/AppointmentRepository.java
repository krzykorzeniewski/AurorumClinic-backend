package pl.edu.pja.aurorumclinic.features.appointments.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("""
    select case
             when exists (
                 select 1
                 from Schedule s
                 join s.services s2
                 where s.doctor.id = :doctorId
                   and s.startedAt <= :startedAt
                   and s.finishedAt >= :finishedAt
                   and s2.id = :serviceId
             )
             and not exists (
                 select 1
                 from Appointment a
                 where a.doctor.id = :doctorId
                   and a.startedAt <= :finishedAt
                   and a.finishedAt >= :startedAt
             )
             then true
             else false
           end
    """)
    boolean timeSlotExists(LocalDateTime startedAt, LocalDateTime finishedAt,
                           Long doctorId, Long serviceId);

    Appointment getAppointmentByIdAndPatientId(Long id, Long patientId);

    @Query("""
            select a from Appointment a
            left join fetch a.doctor d
            left join fetch d.specializations
            left join fetch a.service
            left join fetch a.payment
            where a.patient.id = :patientId
            """)
    Page<Appointment> findAllByPatientId(Long patientId, Pageable pageable);

    List<Appointment> getAllByFinishedAtBeforeAndStatusEquals(
            LocalDateTime finishedAtBefore, AppointmentStatus status);

    List<Appointment> getAllByStartedAtBetweenAndNotificationSentEquals(LocalDateTime startedAtAfter,
                                                                        LocalDateTime startedAtBefore,
                                                                        boolean notificationSent);
}
