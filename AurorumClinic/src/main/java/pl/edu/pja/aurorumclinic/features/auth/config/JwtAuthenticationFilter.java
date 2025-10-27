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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.JwtUtils;

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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        List<String> excludedPaths = List.of(
                "/api/auth/refresh",
                "/api/auth/login",
                "/api/auth/register-patient",
                "/api/auth/reset-password-token",
                "/api/auth/reset-password",
                "/api/auth/login-2fa",
                "/api/auth/login-2fa-token",
                "/api/auth/verify-email",
                "/api/auth/verify-email-token",
                "/api/appointments/guest",
                "/api/doctors/search",
                "/api/doctors/recommended",
                "/api/doctors/*/appointment-slots",
                "/ws",
                "/ws/info"
        );

        boolean isExcluded = excludedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        boolean isGetServices = pathMatcher.match("/api/services", path)
                && method.equalsIgnoreCase("GET");
        return isExcluded || isGetServices;
    }

}
