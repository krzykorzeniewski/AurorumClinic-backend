package pl.edu.pja.aurorumclinic.features.users.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);
}
