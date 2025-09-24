package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @NativeQuery(value = "exec AppointmentSlots :startedAt, :finishedAt, :serviceDuration, :pkDoctor")
    List<Timestamp> appointmentSlots(LocalDateTime startedAt, LocalDateTime finishedAt,
                                     Integer serviceDuration, Integer pkDoctor);

}
