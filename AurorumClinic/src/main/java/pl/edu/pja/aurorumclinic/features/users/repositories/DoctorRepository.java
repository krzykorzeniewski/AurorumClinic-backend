package pl.edu.pja.aurorumclinic.features.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
