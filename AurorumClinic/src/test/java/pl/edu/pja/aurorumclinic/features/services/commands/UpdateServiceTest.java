package pl.edu.pja.aurorumclinic.features.services.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.SpecializationRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.data.models.Specialization;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdateService.class})
@ActiveProfiles("test")
class UpdateServiceTest {

    @MockitoBean
    ServiceRepository serviceRepository;

    @MockitoBean
    SpecializationRepository specializationRepository;

    @Autowired
    UpdateService updateService;

    @Test
    void shouldThrowNotFoundWhenServiceDoesNotExist() {
        Long serviceId = 10L;

        UpdateService.UpdateServiceRequest request =
                new UpdateService.UpdateServiceRequest(
                        "Nowa nazwa",
                        45,
                        new BigDecimal("200.00"),
                        "Opis",
                        Set.of(1L)
                );

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateService.updateService(serviceId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .hasMessageContaining("Id not found");

        verify(serviceRepository).findById(serviceId);
        verifyNoInteractions(specializationRepository);
    }

    @Test
    void shouldThrowApiExceptionWhenSomeSpecializationIdsNotFound() {
        Long serviceId = 10L;

        UpdateService.UpdateServiceRequest request =
                new UpdateService.UpdateServiceRequest(
                        "Nowa nazwa",
                        45,
                        new BigDecimal("200.00"),
                        "Opis",
                        Set.of(1L, 2L)
                );

        Service serviceFromDb = Service.builder()
                .id(serviceId)
                .name("Stara nazwa")
                .duration(30)
                .price(new BigDecimal("150.00"))
                .description("Stary opis")
                .build();

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceFromDb));

        Specialization s1 = Specialization.builder().id(1L).name("Spec1").build();
        when(specializationRepository.findAllById(request.specializationIds()))
                .thenReturn(List.of(s1)); // tylko 1 znaleziony zamiast 2

        assertThatThrownBy(() -> updateService.updateService(serviceId, request))
                .isExactlyInstanceOf(ApiException.class)
                .hasMessageContaining("Some specialization ids are not found");

        verify(serviceRepository).findById(serviceId);
        verify(specializationRepository).findAllById(request.specializationIds());
    }

    @Test
    void shouldUpdateServiceWhenAllDataValid() {
        Long serviceId = 10L;

        UpdateService.UpdateServiceRequest request =
                new UpdateService.UpdateServiceRequest(
                        "Terapia par",
                        90,
                        new BigDecimal("400.50"),
                        "Sesja terapii par",
                        Set.of(1L, 2L)
                );

        Service serviceFromDb = Service.builder()
                .id(serviceId)
                .name("Stara nazwa")
                .duration(30)
                .price(new BigDecimal("150.00"))
                .description("Stary opis")
                .build();

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceFromDb));

        Specialization s1 = Specialization.builder().id(1L).name("Psychologia").build();
        Specialization s2 = Specialization.builder().id(2L).name("Terapia").build();

        when(specializationRepository.findAllById(request.specializationIds()))
                .thenReturn(List.of(s1, s2));

        // when
        var resp = updateService.updateService(serviceId, request);

        // then – response
        assertThat(resp.getBody()).isNotNull();
        ApiResponse<?> body = resp.getBody();
        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNull();

        // wtedy: sprawdzamy, czy encja została zaktualizowana
        assertThat(serviceFromDb.getName()).isEqualTo("Terapia par");
        assertThat(serviceFromDb.getDuration()).isEqualTo(90);
        assertThat(serviceFromDb.getPrice()).isEqualByComparingTo("400.50");
        assertThat(serviceFromDb.getDescription()).isEqualTo("Sesja terapii par");
        assertThat(serviceFromDb.getSpecializations())
                .hasSize(2)
                .extracting(Specialization::getId)
                .containsExactlyInAnyOrder(1L, 2L);

        verify(serviceRepository).findById(serviceId);
        verify(specializationRepository).findAllById(request.specializationIds());
    }
}