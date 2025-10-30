package pl.edu.pja.aurorumclinic.features.auth.verify_phone_number.events;

import pl.edu.pja.aurorumclinic.shared.data.models.Token;
import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record PhoneNumberVerificationTokenCreatedEvent(User user,
                                                       Token token) {
}
