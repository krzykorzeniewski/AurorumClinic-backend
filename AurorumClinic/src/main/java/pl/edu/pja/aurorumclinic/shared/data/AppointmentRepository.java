package pl.edu.pja.aurorumclinic.shared.data;

import jakarta.persistence.Tuple;
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

    @Query("""
            select count(a1.id) as scheduled,
            sum(case when a1.status = 'FINISHED' then 1 else 0 end) as finished,
            avg (case when a1.status = 'FINISHED' then a1.service.duration else null end) as avgDuration,
            avg (case when a1.status = 'FINISHED' then a1.opinion.rating else null end) as avgRating
            from Appointment a1
                    left join a1.opinion
                    left join a1.service
            where a1.doctor.id = :doctorId and (a1.startedAt < :finishedAt and a1.finishedAt > :startedAt)
            """)
    List<Tuple> getDoctorAppointmentStatsBetween(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt);

    @Query("""
            select count(a1.id) as scheduled,
            sum(case when a1.status = 'FINISHED' then 1 else 0 end) as finished,
            avg (case when a1.status = 'FINISHED' then a1.service.duration else null end) as avgDuration,
            avg (case when a1.status = 'FINISHED' then a1.opinion.rating else null end) as avgRating,
            a1.service.name as servName,
            a1.service.id as servId
                from Appointment a1
                    left join a1.opinion
                    left join a1.service
            where a1.doctor.id = :doctorId and (a1.startedAt < :finishedAt and a1.finishedAt > :startedAt)
            group by servName, servId
            order by scheduled desc
            """)
    List<Tuple> getDoctorAppointmentStatsPerServiceBetween(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt);

    @Query("""
            select
                count(a.id) as scheduled,
                sum(case when a.status = 'FINISHED' then 1 else 0 end) as finished,
                avg(case when a.status = 'FINISHED' then a.service.duration else null end) as avgDuration,
                avg(case when a.status = 'FINISHED' then a.opinion.rating else null end) as avgRating
            from Appointment a
            left join a.opinion
            left join a.service
            where (a.startedAt < :finishedAt and a.finishedAt > :startedAt)
    """)
    List<Tuple> getAllAppointmentStatsBetween(LocalDateTime startedAt, LocalDateTime finishedAt);

    boolean existsByService_Schedules_Id(Long scheduleId);
}
