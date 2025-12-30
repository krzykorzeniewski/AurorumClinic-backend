package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.DoctorGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.GetDoctorByIdAbsences;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query("""
            select a from Absence a
            """)
    Page<Absence> findAllPage(Pageable pageable);

    @Query("""
            select a from Absence a where a.doctor.id = :doctorId
            """)
    Page<Absence> findAllByDoctorId(Long doctorId, Pageable pageable);

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.absences.queries.shared.DoctorGetAbsenceResponse(
                a.id, a.name, a.startedAt, a.finishedAt)
           from Absence a
           where a.doctor.id = :doctorId
           """)
    Page<DoctorGetAbsenceResponse> findAllDoctorAbsenceDtos(Long doctorId, Pageable pageable);

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.absences.queries.shared.DoctorGetAbsenceResponse(
                a.id, a.name, a.startedAt, a.finishedAt)
           from Absence a
           where a.doctor.id = :doctorId and a.id = :absenceId
           """)
    Optional<DoctorGetAbsenceResponse> findDoctorAbsenceDtoById(Long absenceId, Long doctorId);
}
