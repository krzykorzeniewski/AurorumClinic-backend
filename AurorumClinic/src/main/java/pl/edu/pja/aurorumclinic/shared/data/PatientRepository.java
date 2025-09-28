package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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


    @Query("""
           select p from Patient p
                      join fetch Appointment a on a.patient.id = p.id
                      join fetch Doctor d on d.id = a.doctor.id
                      join fetch Service s on s.id = a.service.id
           where p.id = :id
           """)
    Optional<Patient> findPatientWithAppointmentsById(Long id);
}
