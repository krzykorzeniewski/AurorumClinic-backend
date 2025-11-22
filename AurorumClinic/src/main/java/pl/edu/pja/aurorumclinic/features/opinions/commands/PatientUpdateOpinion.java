package pl.edu.pja.aurorumclinic.features.opinions.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.moderation.ContentModerationService;

@RestController
@RequestMapping("/api/patients/me/opinions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientUpdateOpinion {

    private final OpinionRepository opinionRepository;
    private final ContentModerationService contentModerationService;

    public record Request(
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 2000) String comment
    ) {}

    @Builder
    public record Response(Long opinionId, int rating, String comment) {}

    @PatchMapping("/{opinionId}")
    @Transactional
    public ResponseEntity<ApiResponse<Response>> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long opinionId,
            @RequestBody @Valid Request req
    ) {
        return ResponseEntity.ok(ApiResponse.success(handle(userId, opinionId, req)));
    }

    private Response handle(Long userId, Long opinionId, Request req) {
        Opinion op = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ApiNotFoundException("Opinion not found", "opinionId"));
        contentModerationService.assertAllowed(req.comment(), "comment");

        if (!op.getAppointment().getPatient().getId().equals(userId)) {
            throw new ApiAuthorizationException("You cannot edit someone else's opinion");
        }

        op.setRating(req.rating());
        op.setComment(req.comment());

        return Response.builder()
                .opinionId(op.getId())
                .rating(op.getRating())
                .comment(op.getComment())
                .build();
    }
}
