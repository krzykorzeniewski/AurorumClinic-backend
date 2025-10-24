package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.services.queries.shared.GetServiceResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.services.queries.shared.GetServiceResponse(
                      s.id, s.name, s.price, s.duration, s.description
                      ) from Service s
           """)
    Page<GetServiceResponse> findAllGetServiceDtos(Pageable pageable);

    @Query("""
           select s from Service s join s.specializations s2
                      where s2.id = :specId
           """)
    Page<Service> getAllServicesBySpecializationId(Long specId, Pageable pageable);

}
