package pl.edu.pja.aurorumclinic.shared.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.pja.aurorumclinic.test_config.IntegrationTest;
import pl.edu.pja.aurorumclinic.test_config.TestDataConfiguration;
import pl.edu.pja.aurorumclinic.shared.data.models.Appointment;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.User;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
public class AppointmentRepositoryIntegrationTest extends IntegrationTest {

    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Test
    void isTimeSlotAvailableShouldReturnTrueWhenScheduleExistsAndNoAppointmentExists() {
        LocalDateTime appointmentStartedAt = LocalDateTime.now().plusHours(6);
        LocalDateTime appointmentFinishedAt = appointmentStartedAt.plusHours(1);
        Long serviceId = 2L;
        Long doctorId = 2L;

        assertThat(appointmentRepository
                .isTimeSlotAvailable(appointmentStartedAt, appointmentFinishedAt, doctorId, serviceId)).isTrue();
    }

    @Test
    void isTimeSlotAvailableShouldReturnFalseWhenScheduleExistsAndAppointmentExists() {
        LocalDateTime appointmentStartedAt = LocalDateTime.now().plusHours(3);
        LocalDateTime appointmentFinishedAt = appointmentStartedAt.plusHours(3);
        Long serviceId = 2L;
        Long doctorId = 2L;

        assertThat(appointmentRepository
                .isTimeSlotAvailable(appointmentStartedAt, appointmentFinishedAt, doctorId, serviceId)).isFalse();
    }

    @Test
    void isTimeSlotAvailableShouldReturnFalseWhenScheduleNotExists() {
        LocalDateTime appointmentStartedAt = LocalDateTime.now().plusDays(2);
        LocalDateTime appointmentFinishedAt = appointmentStartedAt.plusHours(1);
        Long serviceId = 2L;
        Long doctorId = 2L;

        assertThat(appointmentRepository
                .isTimeSlotAvailable(appointmentStartedAt, appointmentFinishedAt, doctorId, serviceId)).isFalse();
    }

    @Test
    void getAppointmentByIdAndPatientIdShouldReturnNullWhenDoesNotExistWithPatientId() {
        Long nonExistingPatientId = 100L;
        Long appointmentId = 1L;

        assertThat(appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, nonExistingPatientId)).isNull();
    }

    @Test
    void getAppointmentByIdAndPatientIdShouldReturnAppointmentWhenExistWithPatientId() {
        Long patientId = 3L;
        Long appointmentId = 1L;

        Appointment resultAppointment =
                appointmentRepository.getAppointmentByIdAndPatientId(appointmentId, patientId);

        assertThat(resultAppointment).isNotNull();
        assertThat(resultAppointment.getPatient().getId()).isEqualTo(patientId);
        assertThat(resultAppointment.getId()).isEqualTo(appointmentId);
    }

    @Test
    void findAllByPatientIdShouldReturnEmptyPageWhenNoExistWithPatientId() {
        Long nonExistingPatientId = 11L;
        Pageable pageable = Pageable.unpaged();

        Page<Appointment> allByPatientId =
                appointmentRepository.findAllByPatientId(nonExistingPatientId, pageable);

        assertThat(allByPatientId).isEmpty();
    }

    @Test
    void findAllByPatientIdShouldReturnPopulatedPageExistWithPatientId() {
        Long patientWithAppointmentsId = 3L;
        Pageable pageable = Pageable.unpaged();

        Page<Appointment> allByPatientId =
                appointmentRepository.findAllByPatientId(patientWithAppointmentsId, pageable);

        assertThat(allByPatientId).isNotEmpty();
        assertThat(allByPatientId).hasSize(2);
        assertThat(allByPatientId)
                .extracting(Appointment::getPatient)
                .extracting(User::getId).containsOnly(patientWithAppointmentsId);
    }

    @Test
    void getAllByFinishedAtBeforeAndStatusEqualsShouldReturnEmptyWhenNoExistsBeforeDateWithStatus() {
        LocalDateTime date = LocalDateTime.now().minusYears(1);
        AppointmentStatus status = AppointmentStatus.CREATED;

        List<Appointment> allByFinishedAtBeforeAndStatusEquals =
                appointmentRepository.getAllByFinishedAtBeforeAndStatusEquals(date, status);

        assertThat(allByFinishedAtBeforeAndStatusEquals).isEmpty();
    }

    @Test
    void getAllByFinishedAtBeforeAndStatusEqualsShouldReturnAppointmentsWhenExistBeforeDateWithStatus() {
        LocalDateTime date = LocalDateTime.now();
        AppointmentStatus status = AppointmentStatus.FINISHED;

        List<Appointment> allByFinishedAtBeforeAndStatusEquals =
                appointmentRepository.getAllByFinishedAtBeforeAndStatusEquals(date, status);

        assertThat(allByFinishedAtBeforeAndStatusEquals).isNotEmpty();
        assertThat(allByFinishedAtBeforeAndStatusEquals).hasSize(2);
        assertThat(allByFinishedAtBeforeAndStatusEquals)
                .extracting(Appointment::getStatus)
                .containsOnly(status);
    }

    @Test
    void getAllByStartedAtBetweenAndNotificationSentEqualsShouldReturnEmptyWhenNoExistsWithStartedAtBetweenAndNotificationSentEqual() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(12);
        LocalDateTime endDate = LocalDateTime.now();
        boolean notificationSent = false;

        List<Appointment> allByStartedAtBetweenAndNotificationSentEquals =
                appointmentRepository.getAllByStartedAtBetweenAndNotificationSentEquals(startDate, endDate, notificationSent);

        assertThat(allByStartedAtBetweenAndNotificationSentEquals).isEmpty();
    }

    @Test
    void getAllByStartedAtBetweenAndNotificationSentEqualsShouldReturnAppointmentsWhenExistsWithStartedAtBetweenAndNotificationSentEqual() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(12);
        LocalDateTime endDate = LocalDateTime.now();
        boolean notificationSent = true;

        List<Appointment> allByStartedAtBetweenAndNotificationSentEquals =
                appointmentRepository.getAllByStartedAtBetweenAndNotificationSentEquals(startDate, endDate, notificationSent);

        assertThat(allByStartedAtBetweenAndNotificationSentEquals).isNotEmpty();
        assertThat(allByStartedAtBetweenAndNotificationSentEquals).hasSize(2);
        assertThat(allByStartedAtBetweenAndNotificationSentEquals)
                .extracting(Appointment::isNotificationSent)
                .containsOnly(notificationSent);
    }

    @Test
    void existsBetweenUsersShouldReturnFalseWhenNotExistsBetweenTwoUserIds() {
        Long doctorId = 1L;
        Long patientId = 4L;

        assertThat(appointmentRepository.existsBetweenUsers(doctorId, patientId)).isFalse();
    }

    @Test
    void existsBetweenUsersShouldReturnTrueWhenExistsBetweenTwoUserIds() {
        Long doctorId = 1L;
        Long patientId = 3L;

        assertThat(appointmentRepository.existsBetweenUsers(doctorId, patientId)).isTrue();
    }

    @Test
    void findAllByScheduleShouldReturnEmptyWhenNoExistForDoctorIdBetweenDates() {
        Schedule scheduleWithNoAppointments = scheduleRepository.findById(3L).orElse(null);

        List<Appointment> allBySchedule = appointmentRepository.findAllBySchedule(scheduleWithNoAppointments.getDoctor().getId(),
                scheduleWithNoAppointments.getStartedAt(), scheduleWithNoAppointments.getFinishedAt());

        assertThat(allBySchedule).isEmpty();
    }

    @Test
    void findAllByScheduleShouldReturnAppointmentsWhenExistForDoctorIdBetweenDates() {
        Schedule scheduleWith2Appointments = scheduleRepository.findById(2L).orElse(null);
        LocalDateTime scheduleStartedAt = scheduleWith2Appointments.getStartedAt();
        LocalDateTime scheduleFinishedAt = scheduleWith2Appointments.getFinishedAt();
        Long scheduleDoctorId = scheduleWith2Appointments.getDoctor().getId();

        List<Appointment> allBySchedule = appointmentRepository.findAllBySchedule(scheduleDoctorId,
                scheduleStartedAt, scheduleFinishedAt);

        assertThat(allBySchedule).isNotEmpty();
        assertThat(allBySchedule).hasSize(2);

        assertThat(allBySchedule)
                .extracting(Appointment::getDoctor)
                .extracting(User::getId)
                .containsOnly(scheduleDoctorId);

        assertThat(allBySchedule)
                .extracting(Appointment::getStartedAt)
                .filteredOn(dateTime ->
                        (dateTime.isAfter(scheduleStartedAt) || dateTime.isEqual(scheduleStartedAt)))
                .hasSize(2);

        assertThat(allBySchedule)
                .extracting(Appointment::getFinishedAt)
                .filteredOn(dateTime ->
                        (dateTime.isBefore(scheduleFinishedAt) || dateTime.isEqual(scheduleFinishedAt)))
                .hasSize(2);
    }

    @Test
    void existsByScheduleShouldReturnFalseWhenNoAppointmentExistForDoctorIdBetweenDates() {
        Schedule scheduleWithNoAppointments = scheduleRepository.findById(3L).orElse(null);
        LocalDateTime scheduleStartedAt = scheduleWithNoAppointments.getStartedAt();
        LocalDateTime scheduleFinishedAt = scheduleWithNoAppointments.getFinishedAt();
        Long scheduleDoctorId = scheduleWithNoAppointments.getDoctor().getId();

        boolean existsBySchedule =
                appointmentRepository.existsBySchedule(scheduleDoctorId, scheduleStartedAt, scheduleFinishedAt);

        assertThat(existsBySchedule).isFalse();
    }

    @Test
    void existsByScheduleShouldReturnFalseWhenNoCreatedAppointmentExistForDoctorIdBetweenDates() {
        Schedule scheduleWith2Appointments = scheduleRepository.findById(2L).orElse(null);
        LocalDateTime scheduleStartedAt = scheduleWith2Appointments.getStartedAt();
        LocalDateTime scheduleFinishedAt = scheduleWith2Appointments.getFinishedAt();
        Long scheduleDoctorId = scheduleWith2Appointments.getDoctor().getId();

        boolean existsBySchedule =
                appointmentRepository.existsBySchedule(scheduleDoctorId, scheduleStartedAt, scheduleFinishedAt);

        assertThat(existsBySchedule).isTrue();
    }

    @Test
    void getAppointmentIdsInPreviousScheduleTimeslotShouldReturnEmptyWhenScheduleHadNoAppointments() {
        Schedule scheduleWithNoAppointments = scheduleRepository.findById(3L).orElse(null);
        LocalDateTime scheduleStartedAt = scheduleWithNoAppointments.getStartedAt();
        LocalDateTime scheduleFinishedAt = scheduleWithNoAppointments.getFinishedAt();
        Long scheduleDoctorId = scheduleWithNoAppointments.getDoctor().getId();

        LocalDateTime newScheduleStartedAt = scheduleStartedAt.plusHours(24);
        LocalDateTime newScheduleFinishedAt = newScheduleStartedAt.minusHours(12);

        Set<Long> appointmentIdsInPreviousScheduleTimeslot = appointmentRepository.getAppointmentIdsInPreviousScheduleTimeslot(scheduleDoctorId, scheduleStartedAt,
                scheduleFinishedAt, newScheduleStartedAt, newScheduleFinishedAt);

        assertThat(appointmentIdsInPreviousScheduleTimeslot).isEmpty();
    }

    @Test
    void getAppointmentIdsInPreviousScheduleTimeslotShouldReturnAppointmentIdsWhenScheduleHadAppointments() {
        Schedule scheduleWith2Appointments = scheduleRepository.findById(2L).orElse(null);
        LocalDateTime scheduleStartedAt = scheduleWith2Appointments.getStartedAt();
        LocalDateTime scheduleFinishedAt = scheduleWith2Appointments.getFinishedAt();
        Long scheduleDoctorId = scheduleWith2Appointments.getDoctor().getId();
        Set<Long> appointmentIdsInSchedule = Set.of(3L, 4L);
        LocalDateTime newScheduleStartedAt = scheduleStartedAt.plusHours(24);
        LocalDateTime newScheduleFinishedAt = newScheduleStartedAt.minusHours(12);

        Set<Long> appointmentIdsInPreviousScheduleTimeslot = appointmentRepository.getAppointmentIdsInPreviousScheduleTimeslot(scheduleDoctorId, scheduleStartedAt,
                scheduleFinishedAt, newScheduleStartedAt, newScheduleFinishedAt);

        assertThat(appointmentIdsInPreviousScheduleTimeslot).isNotEmpty();
        assertThat(appointmentIdsInPreviousScheduleTimeslot).hasSize(2);
        assertThat(appointmentIdsInPreviousScheduleTimeslot)
                .containsExactlyInAnyOrderElementsOf(appointmentIdsInSchedule);
    }

}
