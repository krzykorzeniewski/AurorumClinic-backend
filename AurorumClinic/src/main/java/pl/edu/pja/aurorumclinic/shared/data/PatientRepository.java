package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientAppointmentsResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);
    List<Patient> findByNewsletterTrue();

    @Query("""
           select p from Patient p where
                      lower(p.name) like lower(concat('%', :query, '%')) or
                      lower(p.surname) like lower(concat('%', :query, '%')) or
                      lower(p.email) like lower(concat('%', :query, '%')) or
                      lower(p.pesel) like lower(concat('%', :query, '%')) or
                      lower(p.phoneNumber) like lower(concat('%', :query, '%'))
           """)
    List<Patient> searchAllBySearchParam(String query);

    Optional<GetPatientAppointmentsResponse> findPatientWithAppointmentsById(Long id);
}
