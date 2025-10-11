package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);
    List<Patient> findByNewsletterTrue();

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse(
            p.id, p.name, p.surname, p.pesel, p.birthdate, p.email, p.phoneNumber, p.twoFactorAuth, p.newsletter,
            p.emailVerified, p.phoneNumberVerified, p.communicationPreferences)
              from Patient p where
              lower(p.name) like lower(concat('%', :query, '%')) or
              lower(p.surname) like lower(concat('%', :query, '%')) or
              lower(p.email) like lower(concat('%', :query, '%')) or
              lower(p.pesel) like lower(concat('%', :query, '%')) or
              lower(p.phoneNumber) like lower(concat('%', :query, '%'))
           """)
    Page<GetPatientResponse> searchAllByQuery(String query, Pageable pageable);

    //TODO przeniesc do appointment repository i appointment repository do shared
    @Query("""
        from Patient p 
            left join fetch p.appointments a
            left join fetch a.doctor d
            left join fetch d.specializations
            left join fetch a.service
            left join fetch a.payment
            where p.id = :patientId
        """)
    Page<Appointment> getPatientAppointmentsById(Long patientId, Pageable pageable);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse(
            p.id, p.name, p.surname, p.pesel, p.birthdate, p.email, p.phoneNumber, p.twoFactorAuth, p.newsletter,
            p.emailVerified, p.phoneNumberVerified, p.communicationPreferences)
            from Patient p
            where p.id = :id
            """)
    GetPatientResponse getPatientById(Long id);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse(
            p.id, p.name, p.surname, p.pesel, p.birthdate, p.email, p.phoneNumber, p.twoFactorAuth, p.newsletter,
            p.emailVerified, p.phoneNumberVerified, p.communicationPreferences)
            from Patient p
            """)
    Page<GetPatientResponse> findAllGetPatientDtos(Pageable pageable);
}
