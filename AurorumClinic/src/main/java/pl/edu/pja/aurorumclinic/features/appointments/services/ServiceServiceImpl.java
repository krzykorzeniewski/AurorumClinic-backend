package pl.edu.pja.aurorumclinic.features.appointments.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.request.CreateServiceRequest;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.request.UpdateServiceRequest;
import pl.edu.pja.aurorumclinic.features.appointments.services.dtos.response.GetServiceResponse;
import pl.edu.pja.aurorumclinic.features.appointments.shared.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    @Override
    @Transactional
    public void createService(CreateServiceRequest createServiceRequest) {
        Service service = Service.builder()
                .name(createServiceRequest.name())
                .price(createServiceRequest.price())
                .duration(createServiceRequest.duration())
                .description(createServiceRequest.description())
                .build();
        serviceRepository.save(service);
    }

    @Override
    public Page<GetServiceResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return serviceRepository.findAllGetServiceDtos(pageable);
    }

    @Override
    @Transactional
    public void updateService(Long serviceId, UpdateServiceRequest request) {
        Service serviceFromDb = serviceRepository.findById(serviceId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        serviceFromDb.setName(request.name());
        serviceFromDb.setDuration(request.duration());
        serviceFromDb.setDescription(request.description());
        serviceFromDb.setPrice(request.price());
    }

    @Override
    @Transactional
    public void deleteService(Long serviceId) {
        Service serviceFromDb = serviceRepository.findById(serviceId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        serviceRepository.delete(serviceFromDb);
    }
}
