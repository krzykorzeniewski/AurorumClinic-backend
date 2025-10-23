package pl.edu.pja.aurorumclinic.features.chats.queries;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
public class GetChatsResponse {

    private final Long id;
    private final String name;
    private final String surname;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String profilePicture;
}
