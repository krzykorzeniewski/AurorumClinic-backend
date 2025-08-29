package pl.edu.pja.aurorumclinic.features.appointments.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;

import java.time.LocalDateTime;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("""
            select
                  case when
                  (s.startedAt between :startedAt and :finishedAt) or
                  (s.finishedAt between :startedAt and :finishedAt) or
                  (s.startedAt < :startedAt and s.finishedAt > :finishedAt) or
                  (s.startedAt > :startedAt and s.finishedAt < :finishedAt)
                  then true
                  else false
                  end
            from Schedule s where s.doctor.id = :doctorId
            """)
    boolean scheduleExistsInIntervalForDoctor(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId);

}
