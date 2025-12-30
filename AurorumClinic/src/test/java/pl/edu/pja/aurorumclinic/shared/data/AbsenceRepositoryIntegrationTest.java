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
import pl.edu.pja.aurorumclinic.features.absences.queries.shared.DoctorGetAbsenceResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
public class AbsenceRepositoryIntegrationTest extends IntegrationTest {

    @Autowired
    AbsenceRepository absenceRepository;

    @Test
    void absenceExistsInIntervalForDoctorShouldReturnFalseWhenNoExistsBetweenDatesForDoctorId() {
        LocalDateTime startedAt = LocalDateTime.of(2025, 12, 1, 8, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 12, 24, 21, 0);
        Long doctorId = 1L; //Mariusz has absence between 25 and 26 December

        assertThat(absenceRepository.absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId)).isFalse();
    }

    @Test
    void absenceExistsInIntervalForDoctorShouldReturnTrueWhenExistsBetweenDatesForDoctorId() {
        LocalDateTime startedAt = LocalDateTime.of(2025, 12, 24, 8, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 12, 30, 21, 0);
        Long doctorId = 1L; //Mariusz has absence between 25 and 26 December

        assertThat(absenceRepository.absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctorId)).isTrue();
    }

    @Test
    void absenceExistsInIntervalForDoctorExcludingIdShouldReturnFalseWhenNoOtherExistsBetweenDatesForDoctorId() {
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 1, 8, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 11, 30, 21, 0);
        Long doctorId = 1L;
        Long absenceId = 1L;//Mariusz has absence 11th of November

        assertThat(absenceRepository
                .absenceExistsInIntervalForDoctorExcludingId(startedAt, finishedAt, doctorId, absenceId)).isFalse();
    }

    @Test
    void absenceExistsInIntervalForDoctorExcludingIdShouldReturnTrueWhenOtherExistsBetweenDatesForDoctorId() {
        LocalDateTime startedAt = LocalDateTime.of(2025, 11, 1, 8, 0);
        LocalDateTime finishedAt = LocalDateTime.of(2025, 12, 31, 21, 0);
        Long doctorId = 1L;
        Long absenceId = 1L;

        assertThat(absenceRepository
                .absenceExistsInIntervalForDoctorExcludingId(startedAt, finishedAt, doctorId, absenceId)).isTrue();
        //Mariusz has second absence 25-26th of December
    }

    @Test
    void findAllPageReturnPageOfAbsencesWhenExist() {
        //Mariusz and Błażej have 3 absences total in 2025
        Pageable pageable = Pageable.unpaged();

        assertThat(absenceRepository.findAllPage(pageable))
                .isNotEmpty()
                .hasSize(3);
    }

    @Test
    void findAllByDoctorIdShouldReturnAbsencesWhenExistForDoctorId() {
        Long doctorId = 1L; //Mariusz has absences 11th of November and 25-26th of December
        Pageable pageable = Pageable.unpaged();
        assertThat(absenceRepository.findAllByDoctorId(doctorId, pageable))
                .isNotEmpty()
                .hasSize(2)
                .extracting(absence ->
                        Objects.equals(absence.getDoctor().getId(), doctorId))
                .hasSize(2);
    }

    @Test
    void findAllDoctorAbsenceDtosBetweenShouldReturnAbsenceDtosWhenExistForDoctorIdBetweenDates() {
        Long doctorId = 1L;
        Pageable pageable = Pageable.unpaged();
        Page<Absence> absencesFromDb = absenceRepository.findAllByDoctorId(doctorId, pageable);

        Page<DoctorGetAbsenceResponse> resultPage =
                absenceRepository.findAllDoctorAbsenceDtos(doctorId, pageable);

        assertThat(resultPage)
                .isNotEmpty()
                .hasSize(2)
                .extracting(DoctorGetAbsenceResponse::id)
                .containsExactlyInAnyOrderElementsOf((absencesFromDb.stream().map(Absence::getId).toList()))
                .hasSize(2);

        assertThat(resultPage)
                .extracting(DoctorGetAbsenceResponse::name)
                .containsExactlyInAnyOrderElementsOf((absencesFromDb.stream().map(Absence::getName).toList()))
                .hasSize(2);
    }

    @Test
    void findDoctorAbsenceDtoByIdShouldReturnEmptyWhenNoExistForDoctorIdAndAbsenceId() {
        Long doctorId = 1L;
        Long absenceId = 3L; //that's Błażej's absence, not Mariusz's

        assertThat(absenceRepository.findDoctorAbsenceDtoById(absenceId, doctorId)).isEmpty();
    }

    @Test
    void findDoctorAbsenceDtoByIdShouldReturnAbsenceDtoWhenExistForDoctorIdAndAbsenceId() {
        Long doctorId = 1L;
        Long absenceId = 2L; //that's Mariusz's absence
        Absence absenceFromDb = absenceRepository.findById(absenceId).orElseThrow();

        Optional<DoctorGetAbsenceResponse> dtoById = absenceRepository.findDoctorAbsenceDtoById(absenceId, doctorId);

        assertThat(dtoById).isNotEmpty();
        assertThat(dtoById.get().id()).isEqualTo(absenceId);
        assertThat(dtoById.get().name()).isEqualTo(absenceFromDb.getName());
        assertThat(dtoById.get().startedAt()).isEqualTo(absenceFromDb.getStartedAt());
        assertThat(dtoById.get().finishedAt()).isEqualTo(absenceFromDb.getFinishedAt());
    }


}
