package pl.edu.pja.aurorumclinic.features.statistics;

import jakarta.persistence.Tuple;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stats/patients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetPatientStats {

    private final PatientRepository patientRepository;

    @GetMapping("")
    public ResponseEntity<ApiResponse<GetPatientStatsResponse>> getPatientStats(
            @RequestParam LocalDateTime startedAt,
            @RequestParam LocalDateTime finishedAt
            ) {
        return ResponseEntity.ok(ApiResponse.success(handle(startedAt, finishedAt)));
    }

    private GetPatientStatsResponse handle(LocalDateTime startedAt, LocalDateTime finishedAt) {
        Tuple patientStatsTuple = patientRepository.getPatientStats(startedAt, finishedAt);
        return GetPatientStatsResponse.builder()
                .registered((Long) patientStatsTuple.get("totalRegistered"))
                .registeredThisPeriod((Long) patientStatsTuple.get("registeredThisPeriod"))
                .subscribedToNewsletter((Long) patientStatsTuple.get("subscribedToNewsletter"))
                .build();
    }

    @Builder
    public record GetPatientStatsResponse(Long registered,
                                          Long registeredThisPeriod,
                                          Long subscribedToNewsletter) {

    }

}
