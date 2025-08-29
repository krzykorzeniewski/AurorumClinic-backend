package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
