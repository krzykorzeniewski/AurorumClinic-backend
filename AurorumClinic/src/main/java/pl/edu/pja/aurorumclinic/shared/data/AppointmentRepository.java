package pl.edu.pja.aurorumclinic.shared.data;

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
                   and a.startedAt < :finishedAt
                   and a.finishedAt > :startedAt
             )
             then true
             else false
           end
    """)
    boolean isTimeSlotAvailable(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId, Long serviceId);

    Appointment getAppointmentByIdAndPatientId(Long id, Long patientId);

    Page<Appointment> findAllByPatientId(Long patientId, Pageable pageable);

    List<Appointment> getAllByFinishedAtBeforeAndStatusEquals(
            LocalDateTime finishedAtBefore, AppointmentStatus status);

    List<Appointment> getAllByStartedAtBetweenAndNotificationSentEquals(LocalDateTime startedAtAfter,
                                                                        LocalDateTime startedAtBefore,
                                                                        boolean notificationSent);

    @Query("""
           select case
                 when exists (
                        select 1 from Appointment a
                        where (a.doctor.id = :participantId and a.patient.id = :secParticipantId)
                        or (a.doctor.id = :secParticipantId and a.patient.id = :participantId)
                 )
                 then true
                 else false
           end
           """)
    boolean existsBetweenUsers(Long participantId, Long secParticipantId);

    Page<Appointment> findByService_Schedules_Id(Pageable pageable, Long scheduleId);
}
