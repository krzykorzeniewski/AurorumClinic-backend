package pl.edu.pja.aurorumclinic.features.auth.reset_password.events;

import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record ResetPasswordTokenCreatedEvent(User user,
                                             Token token) {
}
