package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.util.List;

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
    Page<Patient> searchAllBySearchParam(String query, Pageable pageable);

}
