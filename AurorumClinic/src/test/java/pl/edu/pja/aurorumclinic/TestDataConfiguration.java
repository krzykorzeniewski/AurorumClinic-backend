package pl.edu.pja.aurorumclinic;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import pl.edu.pja.aurorumclinic.shared.data.*;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

@TestConfiguration
public class TestDataConfiguration {

    @Autowired
    SpecializationRepository specializationRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    AbsenceRepository absenceRepository;

    @Autowired
    TokenRepository tokenRepository;

    @PostConstruct
    public void init() {
        createTestData();
    }

    public void createTestData() {
        Specialization specialization1 = Specialization.builder()
                .name("Psychiatra dorosłych")
                .build();
        specializationRepository.save(specialization1);

        Specialization specialization2 = Specialization.builder()
                .name("Psychiatra dziecięcy")
                .build();
        specializationRepository.save(specialization2);

        Doctor doctor1 = Doctor.builder()
                .name("Mariusz")
                .surname("Ale to jest męczące o 22:32 w Piątek")
                .email("mariusz@example.com")
                .password("$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa")
                .role(UserRole.DOCTOR)
                .pesel("12312312311")
                .phoneNumber("213742000")
                .emailVerified(true)
                .birthdate(LocalDate.of(1980, 1, 1))
                .description("Super opis Mariusza")
                .education("Wyedukowany jestem bardzo")
                .experience("Doświadczenie też mam spore")
                .pwzNumber("PWZ2137")
                .specializations(new HashSet<>())
                .build();
        doctor1.getSpecializations().add(specialization1);
        doctorRepository.save(doctor1);

        Doctor doctor2 = Doctor.builder()
                .name("Błażej")
                .surname("Ale to jest męczące o 22:34 w Piątek")
                .email("błażej@example.com")
                .password("$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa")
                .role(UserRole.DOCTOR)
                .pesel("00000000000")
                .emailVerified(true)
                .phoneNumber("123420000")
                .birthdate(LocalDate.of(1989, 10, 5))
                .description("Super opis Błażeja")
                .education("Wyedukowany jestem niezbyt bardzo")
                .experience("Doświadczenie też mam... w sumie to nie mam")
                .pwzNumber("PWZ0000000")
                .specializations(new HashSet<>())
                .build();
        doctor2.getSpecializations().add(specialization2);
        doctorRepository.save(doctor2);

        Patient patient1 = Patient.builder()
                .name("Andrzej")
                .surname("Jezus Maria")
                .email("andrzej@example.com")
                .password("$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa")
                .role(UserRole.PATIENT)
                .twoFactorAuth(true)
                .emailVerified(true)
                .phoneNumberVerified(true)
                .pesel("98768892106")
                .phoneNumber("798563386")
                .birthdate(LocalDate.of(1995, 5, 5))
                .newsletter(false)
                .communicationPreferences(CommunicationPreference.EMAIL)
                .build();
        patientRepository.save(patient1);

        Token token1 = Token.builder()
                .name(TokenName.TWO_FACTOR_AUTH)
                .user(patient1)
                .rawValue("123123")
                .value("$2a$12$xWpNg9vh6b9Vt29b4QvSvuAzD8eZoMV4e/I4XIZgRXJ0xsNwQJqUG")
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepository.save(token1);

        Patient patient2 = Patient.builder()
                .name("Maurycy")
                .surname("Kowalski")
                .email("maurycy@example.com")
                .password("$2a$12$hU0UlMz0B316YWUJ36cs9ObnVw34FDj6rc7ppU6VbZ1A/B.pYKSPa")
                .role(UserRole.PATIENT)
                .emailVerified(true)
                .pesel("99999999999")
                .phoneNumber("068512786")
                .birthdate(LocalDate.of(2000, 2, 25))
                .newsletter(false)
                .communicationPreferences(CommunicationPreference.EMAIL)
                .build();
        patientRepository.save(patient2);

        Service service1 = Service.builder()
                .name("Konsultacja psychiatryczna dorosłych")
                .description("Opis konsultacji dorosłych")
                .duration(30)
                .specializations(doctor1.getSpecializations())
                .price(new BigDecimal(350))
                .build();
        serviceRepository.save(service1);

        Service service2 = Service.builder()
                .name("Opis konsultacji dorosłych dzieci")
                .description("Test Service Description")
                .duration(45)
                .specializations(doctor2.getSpecializations())
                .price(new BigDecimal(350))
                .build();
        serviceRepository.save(service2);

        Absence absence1 = Absence.builder()
                .name("Święto Niepodległości - Mariusz")
                .startedAt(LocalDateTime.of(2025, 11, 11, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 11, 11, 21, 0))
                .doctor(doctor1)
                .build();
        absenceRepository.save(absence1);

        Absence absence2 = Absence.builder()
                .name("Święta Bożego Narodzenia - Mariusz")
                .startedAt(LocalDateTime.of(2025, 12, 25, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 12, 26, 21, 0))
                .doctor(doctor1)
                .build();
        absenceRepository.save(absence2);

        Absence absence3 = Absence.builder()
                .name("Święta Bożego Narodzenia - Błażej")
                .startedAt(LocalDateTime.of(2025, 12, 25, 8, 0))
                .finishedAt(LocalDateTime.of(2025, 12, 26, 21, 0))
                .doctor(doctor2)
                .build();
        absenceRepository.save(absence3);

        Schedule schedule1 = Schedule.builder()
                .startedAt(LocalDateTime.now().minusHours(12))
                .finishedAt(LocalDateTime.now())
                .doctor(doctor1)
                .services(new HashSet<>())
                .build();
        schedule1.getServices().add(service1);
        scheduleRepository.save(schedule1);

        Schedule schedule2 = Schedule.builder()
                .startedAt(LocalDateTime.now())
                .finishedAt(LocalDateTime.now().plusHours(12))
                .doctor(doctor2)
                .services(new HashSet<>())
                .build();
        schedule2.getServices().add(service2);
        scheduleRepository.save(schedule2);

        Schedule schedule3 = Schedule.builder()
                .startedAt(LocalDateTime.now().plusDays(1))
                .finishedAt(LocalDateTime.now().plusDays(1).plusHours(12))
                .doctor(doctor2)
                .services(new HashSet<>())
                .build();
        schedule3.getServices().add(service2);
        scheduleRepository.save(schedule3);

        Appointment appointment1 = Appointment.builder()
                .startedAt(LocalDateTime.now().minusHours(10))
                .finishedAt(LocalDateTime.now().minusHours(10).plusMinutes(service1.getDuration()))
                .status(AppointmentStatus.FINISHED)
                .description("Ale ciężary")
                .notificationSent(true)
                .doctor(doctor1)
                .patient(patient1)
                .opinion(null)
                .service(service1)
                .payment(null)
                .build();
        appointmentRepository.save(appointment1);

        Appointment appointment2 = Appointment.builder()
                .startedAt(LocalDateTime.now().minusHours(2))
                .finishedAt(LocalDateTime.now().minusHours(2).plusMinutes(service1.getDuration()))
                .status(AppointmentStatus.FINISHED)
                .description("Nieprzyjemna sytuacja")
                .notificationSent(true)
                .doctor(doctor1)
                .patient(patient1)
                .service(service1)
                .opinion(null)
                .payment(null)
                .build();
        appointmentRepository.save(appointment2);

        Appointment appointment3 = Appointment.builder()
                .startedAt(LocalDateTime.now().plusHours(1))
                .finishedAt(LocalDateTime.now().plusHours(1).plusMinutes(service2.getDuration()))
                .status(AppointmentStatus.CREATED)
                .description("...")
                .notificationSent(false)
                .doctor(doctor2)
                .patient(patient2)
                .opinion(null)
                .service(service2)
                .payment(null)
                .build();
        appointmentRepository.save(appointment3);

        Appointment appointment4 = Appointment.builder()
                .startedAt(LocalDateTime.now().plusHours(3))
                .finishedAt(LocalDateTime.now().plusHours(3).plusMinutes(service2.getDuration()))
                .status(AppointmentStatus.CREATED)
                .description(":(")
                .notificationSent(false)
                .doctor(doctor2)
                .patient(patient2)
                .opinion(null)
                .service(service2)
                .payment(null)
                .build();
        appointmentRepository.save(appointment4);
    }

}
