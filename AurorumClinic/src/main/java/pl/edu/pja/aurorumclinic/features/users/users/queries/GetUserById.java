package pl.edu.pja.aurorumclinic.features.users.users.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetUserById {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetUserResponse>> getUserById(@PathVariable("id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(handle(userId)));
    }

    private GetUserResponse handle(Long userId) {
        GetUserResponse response = userRepository.findUserResponseDtoById(userId);
        if (response == null) {
            throw new ApiNotFoundException("Id not found", "id");
        }
        return response;
    }

}
