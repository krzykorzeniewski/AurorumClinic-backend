package pl.edu.pja.aurorumclinic.shared.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.edu.pja.aurorumclinic.features.specializations.queries.shared.GetSpecializationResponse;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;

import java.util.List;
import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {


    @Query("""
           select new pl.edu.pja.aurorumclinic.features.specializations.queries.shared.
                      GetSpecializationResponse(s.id, s.name) from Specialization s
           """)
    List<GetSpecializationResponse> findAllSpecializationDtos();

    @Query("""
           select new pl.edu.pja.aurorumclinic.features.specializations.queries.shared.
                      GetSpecializationResponse(s.id, s.name) from Specialization s where s.id = :specId
           """)
    Optional<GetSpecializationResponse> findSpecializationDtoById(Long specId);

}
