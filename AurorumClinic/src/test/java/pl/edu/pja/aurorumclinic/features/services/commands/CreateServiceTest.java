package pl.edu.pja.aurorumclinic.features.services.commands;


import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CreateService.class})
@ActiveProfiles("test")
class CreateServiceTest {

    @MockitoBean
    ServiceRepository serviceRepository;

    @MockitoBean
    SpecializationRepository specializationRepository;

    @Autowired
    CreateService createService;

    @Test
    void shouldThrowApiExceptionWhenSomeSpecializationIdsNotFound() {
        CreateService.CreateServiceRequest request =
                new CreateService.CreateServiceRequest(
                        "Konsultacja psychiatryczna",
                        50,
                        new BigDecimal("250.00"),
                        "Wizyta u psychiatry",
                        Set.of(1L, 2L)
                );

        Specialization s1 = Specialization.builder().id(1L).name("Psychiatria").build();
        when(specializationRepository.findAllById(request.specializationIds()))
                .thenReturn(List.of(s1));

        assertThatThrownBy(() -> createService.createService(request))
                .isExactlyInstanceOf(ApiException.class)
                .hasMessageContaining("Some specialization ids are not found");

        verify(specializationRepository).findAllById(request.specializationIds());
        verifyNoInteractions(serviceRepository);
    }

    @Test
    void shouldSaveServiceWhenAllDataValid() {

        CreateService.CreateServiceRequest request =
                new CreateService.CreateServiceRequest(
                        "Konsultacja psychologiczna",
                        60,
                        new BigDecimal("300.00"),
                        "Wizyta u psychologa",
                        Set.of(1L, 2L)
                );

        Specialization s1 = Specialization.builder().id(1L).name("Psychologia").build();
        Specialization s2 = Specialization.builder().id(2L).name("Terapia").build();

        when(specializationRepository.findAllById(request.specializationIds()))
                .thenReturn(List.of(s1, s2));

        when(serviceRepository.save(any(Service.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var resp = createService.createService(request);

        assertThat(resp.getBody()).isNotNull();
        ApiResponse<?> body = resp.getBody();
        assertThat(body.getStatus()).isEqualTo("success");
        assertThat(body.getData()).isNull();

        ArgumentCaptor<Service> captor = ArgumentCaptor.forClass(Service.class);
        verify(serviceRepository).save(captor.capture());
        Service saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Konsultacja psychologiczna");
        assertThat(saved.getDuration()).isEqualTo(60);
        assertThat(saved.getPrice()).isEqualByComparingTo("300.00");
        assertThat(saved.getDescription()).isEqualTo("Wizyta u psychologa");
        assertThat(saved.getSpecializations())
                .hasSize(2)
                .extracting(Specialization::getId)
                .containsExactlyInAnyOrder(1L, 2L);

        verify(specializationRepository).findAllById(request.specializationIds());
    }
}