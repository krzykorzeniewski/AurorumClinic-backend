package pl.edu.pja.aurorumclinic.shared.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.pja.aurorumclinic.test_config.IntegrationTest;
import pl.edu.pja.aurorumclinic.test_config.TestDataConfiguration;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
public class ScheduleRepositoryIntegrationTest extends IntegrationTest {

    @Autowired
    ScheduleRepository scheduleRepository;

    @Test
    void scheduleExistsInIntervalForDoctorShouldReturnFalseWhenDoesNotExistForDoctorIdInInterval() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(2); //non-existing interval
        LocalDateTime finishedAt = startedAt.plusHours(12);
        Long doctorId = 1L;

        assertThat(scheduleRepository.scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId))
                .isFalse();
    }

    @Test
    void scheduleExistsInIntervalForDoctorShouldReturnTrueWhenDoesExistForDoctorIdInInterval() {
        LocalDateTime startedAt = LocalDateTime.now().minusHours(10); //schedule1
        LocalDateTime finishedAt = startedAt.plusHours(12);
        Long doctorId = 1L;

        assertThat(scheduleRepository.scheduleExistsInIntervalForDoctor(startedAt, finishedAt, doctorId))
                .isTrue();
    }

    @Test
    void findAllDoctorIdAndBetweenShouldReturnEmptyWhenNoExistForDoctorIdInInterval() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(2); //non-existing interval
        LocalDateTime finishedAt = startedAt.plusHours(12);
        Long doctorId = 1L;

        assertThat(scheduleRepository.findAllByDoctorIdAndBetween(doctorId, startedAt, finishedAt))
                .isEmpty();
    }

    @Test
    void findAllDoctorIdAndBetweenShouldReturnEmptySchedulesExistForDoctorIdInInterval() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(1); //schedule2, schedule3
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 2L;

        assertThat(scheduleRepository.findAllByDoctorIdAndBetween(doctorId, startedAt, finishedAt))
                .hasSize(2)
                .extracting(Schedule::getDoctor)
                .extracting(User::getId)
                .containsOnly(doctorId);
    }

    @Test
    void findAllSchedulesBetweenDatesShouldReturnEmptyWhenNoExistInInterval() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(100); //non-existing interval
        LocalDateTime finishedAt = startedAt.plusDays(8);
        Pageable pageable = Pageable.unpaged();

        assertThat(scheduleRepository.findAllSchedulesBetweenDates(startedAt, finishedAt, pageable))
                .isEmpty();
    }

    @Test
    void findAllSchedulesBetweenDatesShouldPageOfSchedulesExistInInterval() {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(1); //schedule1, schedule2, schedule3
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Pageable pageable = Pageable.unpaged();

        assertThat(scheduleRepository.findAllSchedulesBetweenDates(startedAt, finishedAt, pageable))
                .hasSize(3)
                .extracting(input ->
                        (input.getStartedAt().isAfter(startedAt) || input.getStartedAt().isEqual(startedAt))
                        && (input.getFinishedAt().isBefore(finishedAt) || input.getFinishedAt().isEqual(finishedAt)))
                .hasSize(3);
    }

    @Test
    void scheduleExistsInIntervalForDoctorExcludingIdShouldReturnFalseWhenOtherScheduleDoesNotExistInIntervalForDoctorId() { //that's kinda long ;)
        LocalDateTime startedAt = LocalDateTime.now().minusDays(1); //schedule1 only
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 1L;
        Long scheduleIdToExclude = 1L;

        assertThat(scheduleRepository
                .scheduleExistsInIntervalForDoctorExcludingId(startedAt, finishedAt, doctorId, scheduleIdToExclude))
                .isFalse();
    }

    @Test
    void scheduleExistsInIntervalForDoctorExcludingIdShouldReturnTrueWhenOtherScheduleExistInIntervalForDoctorId() { //that's kinda long ;)
        LocalDateTime startedAt = LocalDateTime.now().minusDays(1); //schedule2 and schedule3 exist
        LocalDateTime finishedAt = startedAt.plusDays(10);
        Long doctorId = 2L;
        Long scheduleIdToExclude = 2L;

        assertThat(scheduleRepository
                .scheduleExistsInIntervalForDoctorExcludingId(startedAt, finishedAt, doctorId, scheduleIdToExclude))
                .isTrue();
    }

}
