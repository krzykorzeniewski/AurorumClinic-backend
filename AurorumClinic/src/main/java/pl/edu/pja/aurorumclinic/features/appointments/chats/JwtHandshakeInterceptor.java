package pl.edu.pja.aurorumclinic.features.appointments.chats;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.WebUtils;
import pl.edu.pja.aurorumclinic.features.auth.config.JwtAuthenticationToken;
import pl.edu.pja.aurorumclinic.features.auth.shared.ApiAuthenticationException;
import pl.edu.pja.aurorumclinic.shared.JwtUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String jwt = null;
        if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            HttpServletRequest httpRequest = servletServerHttpRequest.getServletRequest();
            if (WebUtils.getCookie(httpRequest, "Access-Token") != null) {
                jwt = Objects.requireNonNull(WebUtils.getCookie(httpRequest, "Access-Token")).getValue();
            }
            if (jwt == null) {
                throw new ApiAuthenticationException("access token not present", "accessToken");
            }
            Long userIdFromJwt;
            String roleFromJwt;
            try {
                userIdFromJwt = jwtUtils.getUserIdFromJwt(jwt);
                roleFromJwt = jwtUtils.getRoleFromJwt(jwt);
            } catch (JwtException jwtException) {
                throw new ApiAuthenticationException("Invalid access token", "accessToken");}
            JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(
                    userIdFromJwt, List.of(new SimpleGrantedAuthority("ROLE_" + roleFromJwt))
            );
            attributes.put("authentication", authenticationToken);
            return true;
        }
        return false;
    }



    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
