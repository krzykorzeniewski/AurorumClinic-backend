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
    public record Response(Page<OpinionDto> opinions,
                           double averageRating,
                           long total) {
    }
    @Builder
    public record OpinionDto(
            Long id,
            int rating,
            String comment,
            String answer,
            LocalDateTime createdAt,
            Long appointmentId
    ) {}

    @GetMapping("/{doctorId}/opinions")
    public ResponseEntity<ApiResponse<Response>> list(
            @PathVariable Long doctorId,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId, pageable)));
    }

    private Response handle(Long doctorId, Pageable pageable) {
        Page<Opinion> page =
                opinionRepository.findByAppointment_Doctor_IdOrderByCreatedAtDesc(doctorId, pageable);

        double avg = page.isEmpty()
                ? 0.0
                : page.stream()
                .mapToInt(Opinion::getRating)
                .average()
                .orElse(0.0);

        double avgRounded = Math.round(avg * 100.0) / 100.0;
        Page<OpinionDto> mapped = page.map(o -> OpinionDto.builder()
                .id(o.getId())
                .rating(o.getRating())
                .comment(o.getComment())
                .answer(o.getAnswer())
                .createdAt(o.getCreatedAt())
                .appointmentId(o.getAppointment() != null ? o.getAppointment().getId() : null)
                .build()
        );

        return new Response(mapped, avgRounded, page.getTotalElements());
    }
}
