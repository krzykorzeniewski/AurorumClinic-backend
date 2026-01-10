package pl.edu.pja.aurorumclinic.features.absences;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.shared.data.AbsenceRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Absence;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AbsenceValidator {

    private final ScheduleRepository scheduleRepository;
    private final AbsenceRepository absenceRepository;

    public void validateTimeslot(LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctor) {
        if (startedAt.isAfter(finishedAt) || finishedAt.isBefore(startedAt)) {
            throw new ApiException("Start date cannot be after end date", "startedAt, finishedAt");
        }
        if (startedAt.isBefore(LocalDateTime.now())) {
            throw new ApiException("Start date cannot be in the past", "startedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(startedAt,
                finishedAt, doctor.getId())) {
            throw new ApiException("Absence overlaps with already existing schedule", "absence");
        }
        if (absenceRepository.absenceExistsInIntervalForDoctor(startedAt, finishedAt, doctor.getId())) {
            throw new ApiException("Absence overlaps with already existing absence", "absence");
        }
    }

    public void validateNewTimeslot(LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctorFromDb,
                                    Absence absenceFromDb) {
        if (startedAt.isAfter(finishedAt) || finishedAt.isBefore(startedAt)) {
            throw new ApiException("Start date cannot be after end date", "startedAt, finishedAt");
        }
        if (startedAt.isBefore(LocalDateTime.now())) {
            throw new ApiException("Start date cannot be in the past", "startedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(startedAt,
                finishedAt, doctorFromDb.getId())) {
            throw new ApiException("Absence overlaps with already existing schedule", "absence");
        }
        if (absenceRepository.absenceExistsInIntervalForDoctorExcludingId(startedAt, finishedAt, doctorFromDb.getId(),
                absenceFromDb.getId())) {
            throw new ApiException("Absence overlaps with already existing absence", "absence");
        }
    }
}
