package pl.edu.pja.aurorumclinic.shared.data;

import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.chats.queries.GetChatsResponse;
import pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;

import java.time.LocalDateTime;
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

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse(
            p.id, p.name, p.surname, p.pesel, p.birthdate, p.email, p.phoneNumber, p.twoFactorAuth, p.newsletter,
            p.emailVerified, p.phoneNumberVerified, p.communicationPreferences)
            from Patient p
            where p.id = :id
            """)
    GetPatientResponse getPatientResponseDtoById(Long id);

    @Query("""
            select new pl.edu.pja.aurorumclinic.features.users.patients.queries.shared.GetPatientResponse(
            p.id, p.name, p.surname, p.pesel, p.birthdate, p.email, p.phoneNumber, p.twoFactorAuth, p.newsletter,
            p.emailVerified, p.phoneNumberVerified, p.communicationPreferences)
            from Patient p
            """)
    Page<GetPatientResponse> findAllGetPatientDtos(Pageable pageable);

    @NativeQuery("""
            select u1.pk_user, u1.name, u1.surname from message join user_ u1 on u1.pk_user = message.fk_sender
            join patient p1 on p1.pk_patient = u1.pk_user
            join user_ u2 on u2.pk_user = message.fk_receiver where u2.pk_user = :doctorId
            union
            select u2.pk_user, u2.name, u2.surname from message join user_ u1 on u1.pk_user = message.fk_sender
            join user_ u2 on u2.pk_user = message.fk_receiver
            join patient p2 on p2.pk_patient = u2.pk_user where u1.pk_user = :doctorId
            """)
    List<GetChatsResponse> findAllWhoHadConversationWithDoctorId(Long doctorId);

    @Query("""
            select
                count(p.id) as totalRegistered,
                count(case when p.createdAt between :startedAt and :finishedAt then 1 end) as registeredThisPeriod,
                count(case when p.newsletter = true then 1 end) as subscribedToNewsletter
            from Patient p
            """)
    Tuple getPatientStats(LocalDateTime startedAt, LocalDateTime finishedAt);
}
