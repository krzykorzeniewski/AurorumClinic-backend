package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.appointments.specializations.queries.shared.GetSpecializationResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {


    @Query("""
           select new pl.edu.pja.aurorumclinic.features.appointments.specializations.queries.shared.
                      GetSpecializationResponse(s.id, s.name) from Specialization s
           """)
    Page<GetSpecializationResponse> findAllSpecializationDtos(Pageable pageable);

}
