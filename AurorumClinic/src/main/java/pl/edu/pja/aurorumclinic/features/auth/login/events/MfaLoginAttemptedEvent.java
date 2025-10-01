package pl.edu.pja.aurorumclinic.features.auth.login.events;

import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record MfaLoginAttemptedEvent(User user) {
}
