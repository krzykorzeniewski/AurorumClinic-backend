package pl.edu.pja.aurorumclinic.features.opinions.queries;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class GetDoctorOpinions {

    private final OpinionRepository opinionRepository;

    public record Response(List<OpinionDto> opinions, double averageRating, int total) {}

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
    public ResponseEntity<ApiResponse<Response>> list(@PathVariable Long doctorId) {
        return ResponseEntity.ok(ApiResponse.success(handle(doctorId)));
    }

    private Response handle(Long doctorId) {
        List<Opinion> list = opinionRepository.findByAppointment_Doctor_IdOrderByCreatedAtDesc(doctorId);

        double avg = list.isEmpty() ? 0.0 : list.stream()
                .mapToInt(Opinion::getRating)
                .average()
                .orElse(0.0);

        double avgRounded = Math.round(avg * 100.0) / 100.0;

        List<OpinionDto> mapped = list.stream()
                .map(o -> OpinionDto.builder()
                        .id(o.getId())
                        .rating(o.getRating())
                        .comment(o.getComment())
                        .answer(o.getAnswer())
                        .createdAt(o.getCreatedAt())
                        .appointmentId(o.getAppointment() != null ? o.getAppointment().getId() : null)
                        .build())
                .toList();

        return new Response(mapped, avgRounded, list.size());
    }
}
