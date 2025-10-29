package pl.edu.pja.aurorumclinic.features.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Tuple;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.AppointmentRepository;
import pl.edu.pja.aurorumclinic.shared.data.DoctorRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/stats/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorGetAppointmentStats {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DoctorAppointmentStatsResponse>> getDoctorAppointmentStats(
            @AuthenticationPrincipal Long doctorId,
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt,
            @RequestParam(required = false) String fetch
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, startedAt, finishedAt, fetch)));
    }

    private DoctorAppointmentStatsResponse handle(Long doctorId, LocalDateTime startedAt, LocalDateTime finishedAt,
                                                                                    String fetch) {
        doctorRepository.findById(doctorId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );

        List<Tuple> doctorTotalAppointmentStats = appointmentRepository
                .getDoctorAppointmentStatsBetween(doctorId, startedAt, finishedAt);
        if (Objects.equals(fetch, "all")) {
            List<Tuple> doctorAppointmentsStatsByService = appointmentRepository
                    .getDoctorAppointmentStatsPerServiceBetween(doctorId, startedAt, finishedAt);
            return DoctorAppointmentStatsResponse.builder()
                    .totalScheduled((Long) doctorTotalAppointmentStats.get(0).get("scheduled"))
                    .totalFinished((Long) doctorTotalAppointmentStats.get(0).get("finished"))
                    .avgDuration((Double) doctorTotalAppointmentStats.get(0).get("avgDuration"))
                    .avgRating((Double) doctorTotalAppointmentStats.get(0).get("avgRating"))
                    .services(doctorAppointmentsStatsByService.stream().map(tuple ->
                            DoctorAppointmentStatsResponse.ServiceAppointmentStatsDto.builder()
                                    .scheduled((Long) tuple.get("scheduled"))
                                    .finished((Long) tuple.get("finished"))
                                    .avgDuration((Double) tuple.get("avgDuration"))
                                    .avgRating((Double) tuple.get("avgRating"))
                                    .service(DoctorAppointmentStatsResponse.ServiceAppointmentStatsDto.ServiceDto.builder()
                                            .id((Long) tuple.get("servId"))
                                            .name((String) tuple.get("servName"))
                                            .build())

                                    .build()).toList())
                    .build();
        }
        else
            return DoctorAppointmentStatsResponse.builder()
                .totalScheduled((Long) doctorTotalAppointmentStats.get(0).get("scheduled"))
                .totalFinished((Long) doctorTotalAppointmentStats.get(0).get("finished"))
                .avgDuration((Double) doctorTotalAppointmentStats.get(0).get("avgDuration"))
                .avgRating((Double) doctorTotalAppointmentStats.get(0).get("avgRating"))
                .services(null).build();
    }

    @Builder
    record DoctorAppointmentStatsResponse(Long totalScheduled,
                                          Long totalFinished,
                                          Double avgDuration,
                                          Double avgRating,
                                          @JsonInclude(JsonInclude.Include.NON_NULL) List<ServiceAppointmentStatsDto> services) {
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
