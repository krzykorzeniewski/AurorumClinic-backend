package pl.edu.pja.aurorumclinic.features.users.users.commands;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UpdateUser { //update doctor ma inne kolumny niz update employee i update patient

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUserById(@PathVariable("id") Long userId,
                                                         @Valid @RequestBody UpdateUserRequest request) {
        return null;
    }

    @Builder
    record UpdateUserRequest() {

    }

}
