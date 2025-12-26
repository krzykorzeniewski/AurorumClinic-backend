package pl.edu.pja.aurorumclinic.features.specialization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.specializations.queries.GetSpecializationByIdServices;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetSpecializationByIdServices.class})
@ActiveProfiles("test")
class GetSpecializationByIdServicesTest {

    @MockitoBean
    SpecializationRepository specializationRepository;

    @MockitoBean
    ServiceRepository serviceRepository;

    @Autowired
    GetSpecializationByIdServices controller;

    @Test
    void shouldThrowNotFoundWhenSpecializationDoesNotExist() {
        Long specId = 10L;
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name").ascending());

        when(specializationRepository.findById(specId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getByIdServices(specId, pageable))
                .isExactlyInstanceOf(ApiNotFoundException.class);

        verify(specializationRepository).findById(specId);
        verifyNoInteractions(serviceRepository);
    }

    @Test
    void shouldReturnMappedServicesWhenSpecializationExists() {
        Long specId = 5L;
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());

        Specialization spec = Specialization.builder()
                .id(specId)
                .name("Psychiatria")
                .build();

        Service s1 = Service.builder()
                .id(1L)
                .name("Konsultacja psychiatryczna")
                .price(new BigDecimal("250.00"))
                .build();

        Service s2 = Service.builder()
                .id(2L)
                .name("Kontrola psychiatryczna")
                .price(new BigDecimal("180.00"))
                .build();

        Page<Service> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

        when(specializationRepository.findById(specId))
                .thenReturn(Optional.of(spec));
        when(serviceRepository.getAllServicesBySpecializationId(specId, pageable))
                .thenReturn(page);

        var resp = controller.getByIdServices(specId, pageable);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<Page<GetSpecializationByIdServices.GetSpecByIdServicesResponse>> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNotNull();
        assertThat(body.getData().getTotalElements()).isEqualTo(2);

        var content = body.getData().getContent();
        assertThat(content).hasSize(2);

        assertThat(content.get(0).id()).isEqualTo(1L);
        assertThat(content.get(0).name()).isEqualTo("Konsultacja psychiatryczna");
        assertThat(content.get(0).price()).isEqualByComparingTo("250.00");

        assertThat(content.get(1).id()).isEqualTo(2L);
        assertThat(content.get(1).name()).isEqualTo("Kontrola psychiatryczna");
        assertThat(content.get(1).price()).isEqualByComparingTo("180.00");

        verify(specializationRepository).findById(specId);
        verify(serviceRepository).getAllServicesBySpecializationId(specId, pageable);
    }
}

