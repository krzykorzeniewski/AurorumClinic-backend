package pl.edu.pja.aurorumclinic.users;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.models.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
