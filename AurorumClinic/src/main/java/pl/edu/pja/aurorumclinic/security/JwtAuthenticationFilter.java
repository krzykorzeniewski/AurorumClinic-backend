package pl.edu.pja.aurorumclinic.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            Map<String, Object> errorMessage = createResponse(request);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorMessage));
        }
    }

    private Map<String, Object> createResponse(HttpServletRequest request) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        errorAttributes.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorAttributes.put("error", "Unauthorized");
        errorAttributes.put("message", "Authentication failed");
        errorAttributes.put("path", request.getRequestURI());
        return errorAttributes;
    }
}
