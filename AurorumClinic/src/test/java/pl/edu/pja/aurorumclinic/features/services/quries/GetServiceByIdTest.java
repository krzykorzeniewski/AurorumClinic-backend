package pl.edu.pja.aurorumclinic.features.services.quries;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.services.queries.GetServiceById;
import pl.edu.pja.aurorumclinic.features.services.queries.shared.GetServiceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetServiceById.class})
@ActiveProfiles("test")
class GetServiceByIdTest {

    @MockitoBean
    ServiceRepository serviceRepository;

    @Autowired
    GetServiceById controller;

    @Test
    void shouldReturnServiceWhenFound() {
        Long serviceId = 10L;

        GetServiceResponse dto = new GetServiceResponse(
                serviceId,
                "Konsultacja psychiatryczna",
                new BigDecimal("250.00"),
                50,
                "Opis usługi"
        );


        when(serviceRepository.findServiceDtoById(serviceId))
                .thenReturn(Optional.of(dto));

        var resp = controller.getServiceById(serviceId);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<GetServiceResponse> body = resp.getBody();
        GetServiceResponse data = body.getData();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(data).isNotNull();
        assertThat(data.id()).isEqualTo(serviceId);
        assertThat(data.name()).isEqualTo("Konsultacja psychiatryczna");

        verify(serviceRepository).findServiceDtoById(serviceId);
    }

    @Test
    void shouldThrowNotFoundWhenServiceMissing() {
        Long serviceId = 999L;

        when(serviceRepository.findServiceDtoById(serviceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getServiceById(serviceId))
                .isExactlyInstanceOf(ApiNotFoundException.class)
                .hasMessageContaining("Id not found");

        verify(serviceRepository).findServiceDtoById(serviceId);
    }
}