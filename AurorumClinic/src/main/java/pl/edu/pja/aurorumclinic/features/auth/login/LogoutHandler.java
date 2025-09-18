package pl.edu.pja.aurorumclinic.features.auth.login;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/auth/logout")
@RequiredArgsConstructor
public class LogoutHandler {

    @GetMapping
    public ResponseEntity<?> logoutUser() {
        HttpCookie accessTokenCookie = ResponseCookie.from("Access-Token", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        HttpCookie refreshTokenCookie = ResponseCookie.from("Refresh-Token", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(null));
    }

}
