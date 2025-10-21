package pl.edu.pja.aurorumclinic.features.newsletter.queries.shared;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetNewsletterMessageResponse(LocalDateTime createdAt,
                                    String subject,
                                    String text,
                                    Boolean approved,
                                    LocalDateTime reviewedAt,
                                    LocalDateTime sentAt,
                                    GetNewsletterMessageResponse.ReviewerDto reviewer) {
    @Builder
    public record ReviewerDto(Long id,
                       String name,
                       String surname) {
    }
}