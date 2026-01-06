package pl.edu.pja.aurorumclinic.features.opinions.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.OpinionRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Opinion;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/admin/me/opinions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeleteOpinion {

    private final OpinionRepository opinionRepository;

    @DeleteMapping("/{opinionId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long opinionId) {
        return ResponseEntity.ok(ApiResponse.success(handle(opinionId)));
    }

    private String handle(Long opinionId) {
        Opinion op = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ApiNotFoundException("Opinion not found", "opinionId"));

        opinionRepository.delete(op);
        return "Opinion deleted successfully";
    }
}
