package pl.edu.pja.aurorumclinic.features.appointments.services;

import org.springframework.data.domain.Page;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.request.CreateServiceRequest;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.request.UpdateServiceRequest;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.response.GetServiceResponse;

public interface ServiceService {
    void createService(CreateServiceRequest createServiceRequest);

    Page<GetServiceResponse> getAll(int page, int size);

    void updateService(Long serviceId, UpdateServiceRequest request);

    void deleteService(Long serviceId);
}
