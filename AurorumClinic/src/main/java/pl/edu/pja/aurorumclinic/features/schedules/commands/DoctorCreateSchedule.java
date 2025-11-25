package pl.edu.pja.aurorumclinic.features.schedules.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.schedules.ScheduleValidator;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.data.ScheduleRepository;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Doctor;
import pl.edu.pja.aurorumclinic.shared.data.models.Schedule;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorCreateSchedule {

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final ServiceRepository serviceRepository;
    private final ScheduleValidator scheduleValidator;

    @PostMapping("/me")
    @Transactional
    public ResponseEntity<ApiResponse<?>> createSchedule(@RequestBody @Valid CreateScheduleRequest createScheduleRequest,
                                                         @AuthenticationPrincipal Long doctorId) {
        handle(createScheduleRequest, doctorId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void handle(CreateScheduleRequest request, Long doctorId) {
        List<Service> servicesFromDb = serviceRepository.findAllById(request.serviceIds);
        if (!servicesFromDb.stream().map(Service::getId).collect(Collectors.toSet()).containsAll(request.serviceIds)) {
            throw new ApiException("Some service ids are not found", "serviceIds");
        }
        Doctor doctorFromDb = doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );

        scheduleValidator.validateTimeslotAndServices(request.startedAt, request.finishedAt, doctorFromDb, servicesFromDb);

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
                                        @NotNull Set<Long> serviceIds) {
    }

}
