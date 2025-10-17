package pl.edu.pja.aurorumclinic.features.appointments.chats.queries;

import java.time.LocalDateTime;

public record GetMessageResponse(Long authorId,
                                 String text,
                                 LocalDateTime sentAt) {
}
