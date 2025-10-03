package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @NativeQuery(value = "exec AppointmentSlots :startedAt, :finishedAt, :serviceDuration, :pkDoctor")
    List<Timestamp> appointmentSlots(LocalDateTime startedAt, LocalDateTime finishedAt,
                                     Integer serviceDuration, Integer pkDoctor);

    @Query("""
           select d from Doctor d where
              lower(d.name) like lower(concat('%', :query, '%')) or
              lower(d.surname) like lower(concat('%', :query, '%')) or
              lower(d.specialization) like lower(concat('%', :query, '%'))
           """)
    Page<Doctor> findAllByQuery(String query, Pageable pageable);
}
