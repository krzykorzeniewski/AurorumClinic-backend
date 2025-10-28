package pl.edu.pja.aurorumclinic.features.schedules;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.features.schedules.commands.EmployeeCreateSchedule;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final ScheduleRepository scheduleRepository;

    @Value("${workday.start.hour}")
    private Integer startOfDay;

    @Value("${workday.end.hour}")
    private Integer endOfDay;

    public void validateSchedule(LocalDateTime startedAt, LocalDateTime finishedAt, Doctor doctor, List<Service> services) {
        validateTimeSlot(startedAt, finishedAt, doctor.getId());
        validateSpecializations(doctor, services);
    }

    private void validateSpecializations(Doctor doctor, List<Service> services) {
        for (Service service: services) {
            for (Specialization specialization: doctor.getSpecializations()) {
                if(specialization.getServices().contains(service)) {
                    return;
                }
            }
        }
        throw new ApiException("Doctor specialization is not assigned to this service", "specialization");
    }

    private void validateTimeSlot(LocalDateTime startedAt, LocalDateTime finishedAt, Long doctorId) {
        if (startedAt.getHour() < startOfDay) {
            throw new ApiException("Start date cannot be before work hours", "startedAt");
        }
        if (finishedAt.getHour() > endOfDay) {
            throw new ApiException("End date cannot be after work hours", "finishedAt");
        }
        if (startedAt.isAfter(finishedAt)) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (finishedAt.isBefore(startedAt)) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(startedAt,
                finishedAt, doctorId)) {
            throw new ApiException("Schedule overlaps with already existing one", "schedule");
        }
    }
    

}
