package pl.edu.pja.aurorumclinic.features.appointments.schedules;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;

import java.time.LocalDateTime;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("""
            select case when count(s) > 0 then true else false end
                from Schedule s
                where s.doctor.id = :doctorId
                  and s.startedAt < :finishedAt
                  and s.finishedAt > :startedAt
            """)
    boolean scheduleExistsInIntervalForDoctor(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId);

}
