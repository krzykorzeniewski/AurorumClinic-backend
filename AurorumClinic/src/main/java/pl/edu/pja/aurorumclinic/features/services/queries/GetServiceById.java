package pl.edu.pja.aurorumclinic.features.services.queries;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.pja.aurorumclinic.features.services.queries.shared.GetServiceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GetServiceById {

    private final ServiceRepository serviceRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetServiceResponse>> getServiceById(@PathVariable("id") Long serviceId) {
        return ResponseEntity.ok(ApiResponse.success(handle(serviceId)));
    }

    private GetServiceResponse handle(Long serviceId) {
        return serviceRepository.findServiceDtoById(serviceId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "serviceId")
        );
    }

}
