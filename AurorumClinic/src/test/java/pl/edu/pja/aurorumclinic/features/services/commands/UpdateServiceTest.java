package pl.edu.pja.aurorumclinic.features.services.commands;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.Service;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UpdateService.class})
@ActiveProfiles("test")
class UpdateServiceTest {

    @MockitoBean
    ServiceRepository serviceRepository;

    @Autowired
    UpdateService updateService;

    @Test
    void shouldThrowNotFoundWhenServiceDoesNotExist() {
        Long serviceId = 10L;

        UpdateService.UpdateServiceRequest request =
                new UpdateService.UpdateServiceRequest(
                        "Nowa nazwa",
                        "Opis"
                );

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateService.updateService(serviceId, request))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .hasMessageContaining("Id not found");

        verify(serviceRepository).findById(serviceId);
    }

    @Test
    void shouldUpdateServiceWhenAllDataValid() {
        Long serviceId = 10L;

        UpdateService.UpdateServiceRequest request =
                new UpdateService.UpdateServiceRequest(
                        "Nowa nazwa",
                        "Opis"
                );

        Service serviceFromDb = Service.builder()
                .id(serviceId)
                .name("Stara nazwa")
                .duration(30)
                .price(new BigDecimal("150.00"))
                .description("Stary opis")
                .build();

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(serviceFromDb));

        var resp = updateService.updateService(serviceId, request);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<?> body = resp.getBody();
        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNull();

        assertThat(serviceFromDb.getName()).isEqualTo(request.name());
        assertThat(serviceFromDb.getDescription()).isEqualTo(request.description());
        verify(serviceRepository).findById(serviceId);
    }
}