package pl.edu.pja.aurorumclinic.features.newsletter.queries.shared;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetNewsletterMessageResponse(
                                    Long id,
                                    LocalDateTime createdAt,
                                    String subject,
                                    String text,
                                    Boolean approved,
                                    LocalDateTime reviewedAt,
                                    LocalDateTime sentAt,
                                    LocalDateTime scheduledAt,
                                    GetNewsletterMessageResponse.ReviewerDto reviewer) {
    @Builder
    public record ReviewerDto(Long id,
                       String name,
                       String surname) {
    }
}