package pl.edu.pja.aurorumclinic.features.users.events;

import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record PendingPhoneNumberCreatedEvent(User user) {
}
