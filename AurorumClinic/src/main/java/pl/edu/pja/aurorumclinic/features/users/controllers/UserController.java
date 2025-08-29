package pl.edu.pja.aurorumclinic.features.users.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.users.services.UserService;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}/basic-info")
    public ResponseEntity<?> getBasicUserInfo(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getBasicUserInfo(id)));
    }

}
