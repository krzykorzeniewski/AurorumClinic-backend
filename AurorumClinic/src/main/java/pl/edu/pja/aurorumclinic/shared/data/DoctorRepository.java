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

    @NativeQuery(value = "exec AppointmentSlots :startedAt, :finishedAt, :pkService, :pkDoctor")
    List<Timestamp> appointmentSlots(LocalDateTime startedAt, LocalDateTime finishedAt, Long pkService, Long pkDoctor);

    @NativeQuery("""
            select d.*, u.* from doctor d join user_ u on u.pk_user = d.pk_doctor
            join specialization_doctor sd on sd.pk_doctor = d.pk_doctor
            join specialization s on s.pk_specialization = sd.pk_specialization
            join specialization_service ss on ss.pk_specialization = s.pk_specialization
            join service s2 on s2.pk_service = ss.pk_service where s2.pk_service = 1 and (
            	u.name like CONCAT('%', :query, '%') or
            	u.surname like CONCAT('%', :query, '%') or
            	s.name like CONCAT('%', :query, '%')
            );
            """)
    Page<Doctor> findAllByQueryAndServiceId(String query, Pageable pageable, Long serviceId);

    @NativeQuery("""
            select d.*, u.* from doctor d join user_ u on u.pk_user = d.pk_doctor
            join specialization_doctor sd on sd.pk_doctor = d.pk_doctor
            join specialization s on s.pk_specialization = sd.pk_specialization
            join specialization_service ss on ss.pk_specialization = s.pk_specialization
            join service s2 on s2.pk_service = ss.pk_service where s2.pk_service = 1;
            """)
    Page<Doctor> findAllByServiceId(Pageable pageable, Long serviceId);
}
