package pl.edu.pja.aurorumclinic.test_config;

import org.springframework.security.test.context.support.WithSecurityContext;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    long id() default 3;

    String email() default "andrzej@example.com";

    UserRole role() default UserRole.PATIENT;

}