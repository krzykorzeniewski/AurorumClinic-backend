package pl.edu.pja.aurorumclinic.test_config;

import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithMockCustomUser(id = 6, email = "krzysztofa@example.com", role = UserRole.EMPLOYEE)
public @interface WithMockCustomEmployee {
}
