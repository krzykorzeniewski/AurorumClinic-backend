package pl.edu.pja.aurorumclinic.features.auth.login.events;

import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record MfaTokenCreatedEvent(Token token,
                                   User user) {
}
