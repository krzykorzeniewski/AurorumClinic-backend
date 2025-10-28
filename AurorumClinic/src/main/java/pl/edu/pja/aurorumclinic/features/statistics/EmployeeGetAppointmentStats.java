package pl.edu.pja.aurorumclinic.features.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Tuple;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.doctors.queries.GetDoctorById;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stats/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeGetAppointmentStats {

    private final AppointmentRepository appointmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<GetAppointmentStatsResponse>> getAppointmentStatistics(
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt
            ) {
        return ResponseEntity.ok(ApiResponse.success(handle(startedAt, finishedAt)));
    }

    private GetAppointmentStatsResponse handle(LocalDateTime startedAt, LocalDateTime finishedAt) {
        List<Tuple> allAppointmentStatisticsBetween = appointmentRepository
                .getAllAppointmentStatisticsBetween(startedAt, finishedAt);
        return GetAppointmentStatsResponse.builder()
                .totalScheduled((Long) allAppointmentStatisticsBetween.get(0).get("scheduled"))
                .totalFinished((Long) allAppointmentStatisticsBetween.get(0).get("finished"))
                .avgDuration((Double) allAppointmentStatisticsBetween.get(0).get("avgDuration"))
                .avgRating((Double) allAppointmentStatisticsBetween.get(0).get("avgRating"))
                .build();
    }

    @Builder
    record GetAppointmentStatsResponse(Long totalScheduled,
                                        Long totalFinished,
                                        Double avgDuration,
                                        Double avgRating,
                                        @JsonInclude(JsonInclude.Include.NON_NULL) List<DoctorAppointmentStatsDto> doctors) {
        @Builder
        record DoctorAppointmentStatsDto(Long scheduled,
                                         Long finished,
                                         Double avgDuration,
                                         Double avgRating,
                                         DoctorDto doctor,
                                         List<ServiceAppointmentStatsDto> services) {
            @Builder
            record DoctorDto(Long id,
                             String name,
                             String surname,
                             List<SpecializationDto> specializations,
                             String profilePicture,
                             int rating) {
                @Builder
                record SpecializationDto(Long id,
                                         String name) {

                }
            }
        }
            @Builder
            record ServiceAppointmentStatsDto(Long scheduled,
                                              Long finished,
                                              Double avgDuration,
                                              Double avgRating,
                                              ServiceDto service) {
                @Builder
                record ServiceDto(Long id,
                                  String name) {

                }
        }
    }

}
