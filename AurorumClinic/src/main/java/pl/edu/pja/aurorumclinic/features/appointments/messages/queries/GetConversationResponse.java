package pl.edu.pja.aurorumclinic.features.appointments.messages.queries;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class GetConversationResponse {

    private final Long id;
    private final String name;
    private final String surname;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profilePicture;
}
