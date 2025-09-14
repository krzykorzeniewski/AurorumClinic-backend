package pl.edu.pja.aurorumclinic.features.appointments.services;

import lombok.RequiredArgsConstructor;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.features.appointments.dtos.CreateServiceRequest;
import pl.edu.pja.aurorumclinic.features.appointments.repositories.ServiceRepository;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;

    @Override
    public void createService(CreateServiceRequest createServiceRequest) {
        Service service = Service.builder()
                .name(createServiceRequest.name())
                .price(createServiceRequest.price())
                .duration(createServiceRequest.duration())
                .description(createServiceRequest.description())
                .build();
        serviceRepository.save(service);
    }
}
