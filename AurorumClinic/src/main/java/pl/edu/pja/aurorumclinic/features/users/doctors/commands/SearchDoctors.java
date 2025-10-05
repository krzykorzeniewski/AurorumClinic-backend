package pl.edu.pja.aurorumclinic.features.users.doctors.commands;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

import java.io.IOException;

//@RestController
//@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class SearchDoctors {

//    @PermitAll
//    @GetMapping("/search")
//    public ResponseEntity<?> searchAllDoctors(@RequestParam String query,
//                                              @RequestParam (defaultValue = "0") int page,
//                                              @RequestParam (defaultValue = "5") int size) throws IOException {
//        return ResponseEntity.ok(ApiResponse.success(doctorService.searchAllDoctors(query, page, size)));
//    }

}
