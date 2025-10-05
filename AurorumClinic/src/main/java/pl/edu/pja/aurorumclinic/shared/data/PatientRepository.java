package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientAppointmentResponse;
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
    Page<Patient> searchAllByQuery(String query, Pageable pageable);

    @Query("""
        select new pl.edu.pja.aurorumclinic.features.users.dtos.response.GetPatientAppointmentResponse(
            a.id,
            a.status,
            a.startedAt,
            new pl.edu.pja.aurorumclinic.features.users.dtos.response.DoctorDto(
                d.id,
                d.name,
                d.surname,
                d.profilePicture,
                d.specialization
            ),
            new pl.edu.pja.aurorumclinic.features.users.dtos.response.ServiceDto(
                s.id,
                s.name,
                s.price
            )
        )
        from Appointment a
        join a.doctor d
        join a.service s
        where a.patient.id = :patientId
    """)
    Page<GetPatientAppointmentResponse> findPatientAppointmentsById(Long patientId, Pageable pageable);
}
