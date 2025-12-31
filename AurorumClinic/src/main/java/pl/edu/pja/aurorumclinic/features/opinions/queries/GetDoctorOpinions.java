package pl.edu.pja.aurorumclinic.features.opinions.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class GetDoctorOpinions {
    private final OpinionRepository opinionRepository;

    @Builder
    public record OpinionDto(
            Long id,
            int rating,
            String comment,
            String answer,
            LocalDateTime createdAt,
            PatientDto patient
    ) {}

    @Builder
    public record PatientDto(Long id,
                      String name,
                      String surname) {
    }


    @GetMapping("/{doctorId}/opinions")
    public ResponseEntity<ApiResponse<Page<OpinionDto>>> list(
            @PathVariable Long doctorId,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, pageable)));
    }

    private Page<OpinionDto> handle(Long doctorId, Pageable pageable) {
        Page<Opinion> page =
                opinionRepository.findByAppointment_Doctor_IdOrderByCreatedAtDesc(doctorId, pageable);

        return page.map(o -> OpinionDto.builder()
                .id(o.getId())
                .rating(o.getRating())
                .comment(o.getComment())
                .answer(o.getAnswer())
                .createdAt(o.getCreatedAt())
                .patient(PatientDto.builder()
                        .id(o.getAppointment().getPatient().getId())
                        .name(o.getAppointment().getPatient().getName())
                        .surname(o.getAppointment().getPatient().getSurname())
                        .build())
                .build()
        );
    }
}