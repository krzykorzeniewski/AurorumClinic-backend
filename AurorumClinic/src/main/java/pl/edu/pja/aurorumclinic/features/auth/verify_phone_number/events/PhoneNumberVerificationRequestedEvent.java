package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number.events;

import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record PhoneNumberVerificationRequestedEvent(User user) {
}
