package pl.edu.pja.aurorumclinic.features.appointments.services;

import pl.edu.pja.aurorumclinic.features.appointments.dtos.CreateServiceRequest;

public interface ServiceService {
    void createService(CreateServiceRequest createServiceRequest);
}
