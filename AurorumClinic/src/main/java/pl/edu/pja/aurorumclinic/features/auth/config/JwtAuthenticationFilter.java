package pl.edu.pja.aurorumclinic.features.auth.config;

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
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.features.auth.shared.JwtUtils;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException, AuthenticationException {
        String jwt = null;
        if (WebUtils.getCookie(request, "Access-Token") != null) {
            jwt = Objects.requireNonNull(WebUtils.getCookie(request, "Access-Token")).getValue();
        }
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userIdFromJwt;
        String roleFromJwt;
        try {
            userIdFromJwt = jwtUtils.getUserIdFromJwt(jwt);
            roleFromJwt = jwtUtils.getRoleFromJwt(jwt);
        } catch (JwtException jwtException) {
            if (jwtException instanceof ExpiredJwtException) {
                response.setHeader("Token-expired", "true");
            }
            throw new ApiAuthenticationException("Invalid access token", "accessToken");
        }
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                userIdFromJwt, List.of(new SimpleGrantedAuthority("ROLE_" + roleFromJwt))
        );
        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(newContext);
        filterChain.doFilter(request, response);
    }

}
