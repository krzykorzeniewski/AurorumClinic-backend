package pl.edu.pja.aurorumclinic.features.appointments.services;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.shared.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;

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
}
