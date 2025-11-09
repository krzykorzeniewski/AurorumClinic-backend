package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;

import java.time.LocalDateTime;

public interface AbsenceRepository extends JpaRepository<Absence, Long> {

    @Query("""
            select case when count(a) > 0 then true else false end
                from Absence a
                where a.doctor.id = :doctorId
                  and a.startedAt < :finishedAt
                  and a.finishedAt > :startedAt
            """)
    boolean absenceExistsInIntervalForDoctor(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId);

    @Query("""
            select case when count(a) > 0 then true else false end
                from Absence a
                where a.doctor.id = :doctorId
                  and a.startedAt < :finishedAt
                  and a.finishedAt > :startedAt
                  and a.id != :absenceId
            """)
    boolean absenceExistsInIntervalForDoctorExcludingId(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId,
                                                        Long absenceId);
}
