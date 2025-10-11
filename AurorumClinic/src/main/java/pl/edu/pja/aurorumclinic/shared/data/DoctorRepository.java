package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.shared.GetDoctorResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @NativeQuery(value = "exec AppointmentSlots :startedAt, :finishedAt, :pkService, :pkDoctor")
    List<Timestamp> appointmentSlots(LocalDateTime startedAt, LocalDateTime finishedAt, Long pkService, Long pkDoctor);

    @Query("""
           select d from Doctor d
           left join fetch d.specializations s
           where
           lower(d.name) like lower(concat('%', :query, '%')) or
           lower(d.surname) like lower(concat('%', :query, '%')) or
           lower(s.name) like lower(concat('%', :query, '%'))
           """)
    Page<Doctor> findAllByQuery(String query, Pageable pageable);

}
