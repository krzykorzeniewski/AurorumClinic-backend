package pl.edu.pja.aurorumclinic.features.appointments.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.appointments.dtos.CreateScheduleRequest;
import pl.edu.pja.aurorumclinic.features.appointments.repositories.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    @Value("${workday.start.hour}")
    private Integer startOfDay;

    @Value("${workday.end.hour}")
    private Integer endOfDay;

    @Override
    public void createSchedule(CreateScheduleRequest createScheduleRequest) {
        Doctor doctorFromDb = doctorRepository.findById(createScheduleRequest.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (createScheduleRequest.startedAt().getHour() < startOfDay) {
            throw new ApiException("Start date cannot be before work hours", "startedAt");
        }
        if (createScheduleRequest.finishedAt().getHour() > endOfDay) {
            throw new ApiException("End date cannot be after work hours", "finishedAt");
        }
        if (createScheduleRequest.startedAt().isAfter(createScheduleRequest.finishedAt())) {
            throw new ApiException("Start date cannot be after end date", "startedAt");
        }
        if (createScheduleRequest.finishedAt().isBefore(createScheduleRequest.startedAt())) {
            throw new ApiException("End date cannot be before start date", "finishedAt");
        }
        if (scheduleRepository.scheduleExistsInIntervalForDoctor(createScheduleRequest.startedAt(),
                createScheduleRequest.finishedAt(), createScheduleRequest.doctorId())) {
            throw new ApiException("Schedule overlapps with already existing one", "schedule");
        }

        Schedule schedule = Schedule.builder()
                .doctor(doctorFromDb)
                .startedAt(createScheduleRequest.startedAt())
                .finishedAt(createScheduleRequest.finishedAt())
                .build();
        scheduleRepository.save(schedule);
    }
}
