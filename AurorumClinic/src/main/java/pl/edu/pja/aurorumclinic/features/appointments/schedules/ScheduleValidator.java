package pl.edu.pja.aurorumclinic.features.appointments.schedules;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.edu.pja.aurorumclinic.features.appointments.schedules.commands.CreateSchedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleValidator {

    private final ScheduleRepository scheduleRepository;

    @Value("${workday.start.hour}")
    private Integer startOfDay;

    @Value("${workday.end.hour}")
    private Integer endOfDay;

    public void validateSchedule(CreateSchedule.CreateScheduleRequest request, Doctor doctor, List<Service> services) {
        validateTimeSlot(request);
        validateSpecializations(doctor, services);
    }

    private void validateSpecializations(Doctor doctor, List<Service> services) {
        int counter = 0;
        for (Specialization specialization : doctor.getSpecializations()) {
            for (Service service : services) {
                if(specialization.getServices().contains(service)) {
                    counter++;
                }
            }
        }
        if (counter == 0) {
            throw new ApiException("Doctor specialization is not assigned to this service", "specialization");
        }
    }

    private void validateTimeSlot(CreateSchedule.CreateScheduleRequest request) {
        if (request.startedAt().getHour() < startOfDay) {
            throw new ApiException("Start date cannot be before work hours", "startedAt");
        }
        if (request.finishedAt().getHour() > endOfDay) {
            throw new ApiException("End date cannot be after work hours", "finishedAt");
        }
        if (request.startedAt().isAfter(request.finishedAt())) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (request.finishedAt().isBefore(request.startedAt())) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(request.startedAt(),
                request.finishedAt(), request.doctorId())) {
            throw new ApiException("Schedule overlaps with already existing one", "schedule");
        }
    }
    

}
