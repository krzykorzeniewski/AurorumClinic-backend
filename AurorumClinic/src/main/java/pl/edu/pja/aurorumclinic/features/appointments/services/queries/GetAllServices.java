package pl.edu.pja.aurorumclinic.features.appointments.services.queries;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.appointments.services.queries.shared.GetServiceResponse;
import pl.edu.pja.aurorumclinic.features.appointments.shared.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class GetAllServices {

    private final ServiceRepository serviceRepository;

    @PermitAll
    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<GetServiceResponse>>> getAllServices(
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam (defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(handle(page, size)));
    }

    private Page<GetServiceResponse> handle(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return serviceRepository.findAllGetServiceDtos(pageable);
    }

}
