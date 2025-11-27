package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("""
            select case when count(s) > 0 then true else false end
                from Schedule s
                where s.doctor.id = :doctorId
                  and s.startedAt < :finishedAt
                  and s.finishedAt > :startedAt
            """)
    boolean scheduleExistsInIntervalForDoctor(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId);

    @Query("""
           select s
                from Schedule s
                join fetch s.services
                where s.doctor.id = :doctorId
                  and s.startedAt <= :finishedAt
                  and s.finishedAt >= :startedAt
           """)
    List<Schedule> findAllByDoctorIdAndBetween(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt);

    @Query("""
            select s
                from Schedule s
                where s.startedAt >= :startedAt
                  and s.finishedAt <= :finishedAt
            """)
    Page<Schedule> findAllSchedulesBetweenDates(LocalDateTime startedAt, LocalDateTime finishedAt, Pageable pageable);

    @Query("""
            select case when count(s) > 0 then true else false end
                from Schedule s
                where s.doctor.id = :doctorId
                  and s.startedAt < :finishedAt
                  and s.finishedAt > :startedAt
                  and s.id != :scheduleId
            """)
    boolean scheduleExistsInIntervalForDoctorExcludingId(LocalDateTime startedAt, LocalDateTime finishedAt,
                                                         Long doctorId, Long scheduleId);
}
