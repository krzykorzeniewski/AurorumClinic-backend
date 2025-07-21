package pl.edu.pja.aurorumclinic.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityUtils securityUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        String emailFromJwt;
        String roleFromJwt;
        try {
            String jwt = authHeader.substring(7);
            emailFromJwt = securityUtils.getEmailFromJwt(jwt);
            roleFromJwt = securityUtils.getRoleFromJwt(jwt);
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                emailFromJwt, List.of(new SimpleGrantedAuthority(roleFromJwt))
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
        } catch (JwtException jwtException) {
            if (jwtException instanceof ExpiredJwtException) {
                response.setHeader("Token-expired", "true");
            }
            ProblemDetail error = createResponse(request);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write(new ObjectMapper().writeValueAsString(error));
        }
    }

    private ProblemDetail createResponse(HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Unauthorized");
        problemDetail.setDetail("Authentication failed");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return problemDetail;
    }
}
