package pl.edu.pja.aurorumclinic.features.specialization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.specializations.queries.GetAllSpecializations;
import pl.edu.pja.aurorumclinic.features.specializations.queries.shared.GetSpecializationResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {GetAllSpecializations.class})
@ActiveProfiles("test")
class GetAllSpecializationsTest {

    @MockitoBean
    SpecializationRepository specializationRepository;

    @Autowired
    GetAllSpecializations controller;

    @Test
    void shouldReturnSpecializations() {
        GetSpecializationResponse s1 = new GetSpecializationResponse(
                1L,
                "Psychiatria"
        );
        GetSpecializationResponse s2 = new GetSpecializationResponse(
                2L,
                "Psychologia"
        );


        when(specializationRepository.findAllSpecializationDtos())
                .thenReturn(List.of(s1, s2));

        var resp = controller.getAll();

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<List<GetSpecializationResponse>> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData())
                .hasSize(2)
                .containsExactly(s1, s2);
    }
}
