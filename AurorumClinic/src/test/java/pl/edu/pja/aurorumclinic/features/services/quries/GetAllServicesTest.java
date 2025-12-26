package pl.edu.pja.aurorumclinic.features.services.quries;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.pja.aurorumclinic.features.services.queries.GetAllServices;
import pl.edu.pja.aurorumclinic.features.services.queries.shared.GetServiceResponse;
import pl.edu.pja.aurorumclinic.shared.ApiResponse;
import pl.edu.pja.aurorumclinic.shared.data.ServiceRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GetAllServices.class})
@ActiveProfiles("test")
class GetAllServicesTest {

    @MockitoBean
    ServiceRepository serviceRepository;

    @Autowired
    GetAllServices controller;

    @Test
    void shouldReturnPagedServices() {
        // given
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name").ascending());

        GetServiceResponse s1 = new GetServiceResponse(
                1L,
                "Konsultacja psychiatryczna",
                new BigDecimal("250.00"), // price
                50,                       // duration
                "Opis 1"
        );

        GetServiceResponse s2 = new GetServiceResponse(
                2L,
                "Konsultacja psychologiczna",
                new BigDecimal("300.00"),
                60,
                "Opis 2"
        );

        Page<GetServiceResponse> page =
                new PageImpl<>(List.of(s1, s2), pageable, 2);

        when(serviceRepository.findAllGetServiceDtos(pageable)).thenReturn(page);

        // when
        var resp = controller.getAllServices(pageable);

        // then
        assertThat(resp.getBody()).isNotNull();
        ApiResponse<Page<GetServiceResponse>> body = resp.getBody();

        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNotNull();

        Page<GetServiceResponse> dataPage = body.getData();
        assertThat(dataPage.getTotalElements()).isEqualTo(2);
        assertThat(dataPage.getContent()).hasSize(2);

        assertThat(dataPage.getContent().get(0).id()).isEqualTo(1L);
        assertThat(dataPage.getContent().get(0).name()).isEqualTo("Konsultacja psychiatryczna");

        verify(serviceRepository).findAllGetServiceDtos(pageable);
    }

    @Test
    void shouldReturnEmptyPageWhenNoServices() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<GetServiceResponse> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(serviceRepository.findAllGetServiceDtos(pageable)).thenReturn(emptyPage);

        var resp = controller.getAllServices(pageable);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<Page<GetServiceResponse>> body = resp.getBody();
        Page<GetServiceResponse> data = body.getData();

        assertThat(data.getTotalElements()).isZero();
        assertThat(data.getContent()).isEmpty();

        verify(serviceRepository).findAllGetServiceDtos(pageable);
    }
}
