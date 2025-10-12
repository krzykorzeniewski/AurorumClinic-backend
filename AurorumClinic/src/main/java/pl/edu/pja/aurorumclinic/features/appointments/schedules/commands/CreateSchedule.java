package pl.edu.pja.aurorumclinic.features.appointments.schedules.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.schedules.ScheduleRepository;
import pl.edu.pja.aurorumclinic.features.appointments.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.features.appointments.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DOCTOR', 'EMPLOYEE')")
public class CreateSchedule {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final ScheduleValidator scheduleValidator;

    @PostMapping("")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createSchedule(@RequestBody @Valid CreateScheduleRequest createScheduleRequest) {
        handle(createScheduleRequest);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(CreateScheduleRequest request) {
        List<Service> servicesFromDb = serviceRepository.findAllById(request.serviceIds);
        if (servicesFromDb.size() > request.serviceIds.size()) {
            throw new ApiException("Some service ids are not found", "serviceIds");
        }
        Doctor doctorFromDb = doctorRepository.findById(request.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );

        scheduleValidator.validateSchedule(request, doctorFromDb, servicesFromDb);

        Schedule schedule = Schedule.builder()
                .doctor(doctorFromDb)
                .startedAt(request.startedAt())
                .finishedAt(request.finishedAt())
                .services(new HashSet<>(servicesFromDb))
                .build();
        scheduleRepository.save(schedule);
    }

    public record CreateScheduleRequest(@NotNull LocalDateTime startedAt,
                                        @NotNull LocalDateTime finishedAt,
                                        @NotNull Long doctorId,
                                        @NotNull Set<Long> serviceIds) {
    }

}
