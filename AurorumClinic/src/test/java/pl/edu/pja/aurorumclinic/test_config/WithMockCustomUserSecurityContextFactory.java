package pl.edu.pja.aurorumclinic.test_config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import pl.edu.pja.aurorumclinic.features.auth.config.JwtAuthenticationToken;

import java.util.List;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                customUser.id(), List.of(new SimpleGrantedAuthority("ROLE_" + customUser.role()))
        );
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
        return context;
    }
}