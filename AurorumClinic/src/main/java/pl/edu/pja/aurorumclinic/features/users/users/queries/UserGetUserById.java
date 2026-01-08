package pl.edu.pja.aurorumclinic.features.users.users.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetMeByIdResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;


@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserGetUserById {

    private final UserRepository userRepository;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<GetMeByIdResponse>> meGetUserById(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(handle(userId)));
    }

    private GetMeByIdResponse handle(Long userId) {
        return userRepository.getMeByIdResponseDto(userId);
    }

}
