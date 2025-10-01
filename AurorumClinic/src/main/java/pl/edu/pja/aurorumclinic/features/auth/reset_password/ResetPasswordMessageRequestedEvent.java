package pl.edu.pja.aurorumclinic.features.auth.reset_password;

import pl.edu.pja.aurorumclinic.shared.data.models.User;

public record ResetPasswordMessageRequestedEvent(User user) {
}
