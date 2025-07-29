package pl.edu.pja.aurorumclinic.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import pl.edu.pja.aurorumclinic.security.exceptions.InvalidAccessTokenException;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityUtils securityUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException, AuthenticationException {
        String jwt = null;
        if (WebUtils.getCookie(request, "Access-Token") != null) {
            jwt = WebUtils.getCookie(request, "Access-Token").getValue();
        }
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String emailFromJwt;
        String roleFromJwt;
        try {
            emailFromJwt = securityUtils.getEmailFromJwt(jwt);
            roleFromJwt = securityUtils.getRoleFromJwt(jwt);
        } catch (JwtException jwtException) {
            if (jwtException instanceof ExpiredJwtException) {
                response.setHeader("Token-expired", "true");
            }
            throw new InvalidAccessTokenException(jwtException.getLocalizedMessage());
        }
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                emailFromJwt, List.of(new SimpleGrantedAuthority(roleFromJwt))
        );
        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(newContext);
        filterChain.doFilter(request, response);
    }

}
