package pl.edu.pja.aurorumclinic.features.auth.register.events;

import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record AccountVerificationRequestedEvent(User user) {
}
