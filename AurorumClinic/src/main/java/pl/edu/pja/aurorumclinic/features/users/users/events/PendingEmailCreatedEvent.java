package pl.edu.pja.aurorumclinic.features.users.users.events;

import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record PendingEmailCreatedEvent(User user) {
}
