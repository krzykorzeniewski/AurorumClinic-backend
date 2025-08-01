package pl.edu.pja.aurorumclinic.users;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.models.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);
}
