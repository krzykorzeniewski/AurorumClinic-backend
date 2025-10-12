package pl.edu.pja.aurorumclinic.features.users.users.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.users.queries.shared.GetUserResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetAllUsers {

    public ResponseEntity<ApiResponse<Page<GetUserResponse>>> getAllUsers(@PageableDefault Pageable pageable,
                                                                          @RequestParam(required = false) String role,
                                                                          @RequestParam(required = false) String query) {

        return null;
    }

}
