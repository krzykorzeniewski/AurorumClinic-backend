package pl.edu.pja.aurorumclinic.features.appointments.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.appointments.services.queries.shared.GetServiceResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.appointments.services.queries.shared.GetServiceResponse(
                      s.name, s.price, s.duration, s.description
                      ) from Service s
           """)
    Page<GetServiceResponse> findAllGetServiceDtos(Pageable pageable);

}
