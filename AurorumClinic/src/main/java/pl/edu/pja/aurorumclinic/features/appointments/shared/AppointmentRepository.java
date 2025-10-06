package pl.edu.pja.aurorumclinic.features.appointments.shared;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PatientGetAppointmentResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;

import java.time.LocalDateTime;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
    select case
             when exists (
                 select 1
                 from Schedule s
                 where s.doctor.id = :doctorId
                   and s.startedAt <= :startedAt
                   and s.finishedAt >= :finishedAt
             )
             and not exists (
                 select 1
                 from Appointment a
                 where a.doctor.id = :doctorId
                   and a.startedAt <= :finishedAt
                   and a.finishedAt >= :startedAt
             )
             then true
             else false
           end
    """)
    boolean timeSlotExists(LocalDateTime startedAt, LocalDateTime finishedAt,
                           Long doctorId, Long serviceId);


    @Query("""
        select new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PatientGetAppointmentResponse(
            a.id,
            a.status,
            a.startedAt,
            new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.DoctorDto(
                d.id,
                d.name,
                d.surname,
                d.profilePicture,
                d.specialization
            ),
            new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.ServiceDto(
                s.id,
                s.name,
                s.price
            ),
            new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PaymentDto(
                p.id,
                p.amount,
                p.status
            )
        )
        from Appointment a
        join a.doctor d
        join a.service s
        join a.payment p
        where a.patient.id = :patientId and a.id = :appointmentId
    """)
    PatientGetAppointmentResponse getPatientAppointmentById(Long patientId, Long appointmentId);

    @Query("""
        select new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PatientGetAppointmentResponse(
            a.id,
            a.status,
            a.startedAt,
            new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.DoctorDto(
                d.id,
                d.name,
                d.surname,
                d.profilePicture,
                d.specialization
            ),
            new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.ServiceDto(
                s.id,
                s.name,
                s.price
            ),
            new pl.edu.pja.aurorumclinic.features.appointments.patients.queries.shared.PaymentDto(
                p.id,
                p.amount,
                p.status
            )
        )
        from Appointment a
        join a.doctor d
        join a.service s
        join a.payment p
        where a.patient.id = :patientId
        order by a.status desc
    """)
    Page<PatientGetAppointmentResponse> getAllPatientAppointments(Long patientId, Pageable pageable);
}
