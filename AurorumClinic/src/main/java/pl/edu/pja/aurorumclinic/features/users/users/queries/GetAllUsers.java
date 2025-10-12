package pl.edu.pja.aurorumclinic.features.users.users.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.UserRole;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetAllUsers {

    private final UserRepository userRepository;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetUserResponse>>> getAllUsers(@PageableDefault Pageable pageable,
                                                                          @RequestParam(required = false) String role,
                                                                          @RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.success(handle(pageable, role, query)));
    }

    private Page<GetUserResponse> handle(Pageable pageable, String role, String query) {
        UserRole roleAsEnum = null;
        if (role != null) {
            try {
                roleAsEnum = UserRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                roleAsEnum = null;
            }
        }
        Page<GetUserResponse> response;
        if (query == null) {
            response = userRepository.findAllUserResponseDtos(pageable, roleAsEnum);
        } else {
            response = userRepository.searchAllUserResponseDtos(pageable, query, roleAsEnum);
        }
        return response;
    }

}
