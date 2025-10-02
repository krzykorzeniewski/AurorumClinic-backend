package pl.edu.pja.aurorumclinic.features.appointments.patients;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.CreateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.request.UpdateAppointmentPatientRequest;
import pl.edu.pja.aurorumclinic.features.appointments.patients.dtos.response.GetAppointmentPatientResponse;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentCreatedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentDeletedEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.events.AppointmentRescheduledEvent;
import pl.edu.pja.aurorumclinic.features.appointments.shared.ServiceRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentRepository;
import pl.edu.pja.aurorumclinic.features.appointments.shared.AppointmentValidator;
import pl.edu.pja.aurorumclinic.shared.data.UserRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.*;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.AppointmentStatus;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiAuthorizationException;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;
import pl.edu.pja.aurorumclinic.shared.services.ObjectStorageService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class AppointmentPatientServiceImpl implements AppointmentPatientService {

    private final AppointmentValidator appointmentValidator;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectStorageService objectStorageService;

    @Override
    @Transactional
    public void createAppointment(CreateAppointmentPatientRequest createAppointmentPatientRequest, Long userId) {
        Patient patientFromDb = (Patient) userRepository.findById(userId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Doctor doctorFromDb = (Doctor) userRepository.findById(createAppointmentPatientRequest.doctorId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Service serviceFromDb = serviceRepository.findById(createAppointmentPatientRequest.serviceId()).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        Appointment newAppointment = Appointment.builder()
                .service(serviceFromDb)
                .description(createAppointmentPatientRequest.description())
                .patient(patientFromDb)
                .status(AppointmentStatus.CREATED)
                .doctor(doctorFromDb)
                .startedAt(createAppointmentPatientRequest.startedAt())
                .finishedAt(createAppointmentPatientRequest.startedAt().plusMinutes(serviceFromDb.getDuration()))
                .build();
        appointmentValidator.validateTimeSlot(newAppointment.getStartedAt(), newAppointment.getFinishedAt(),
                newAppointment.getDoctor().getId(), newAppointment.getService().getId());

        Appointment appointmentFromDb = appointmentRepository.save(newAppointment);
        applicationEventPublisher.publishEvent(
                new AppointmentCreatedEvent(patientFromDb, appointmentFromDb));
    }

    @Override
    @Transactional
    public void updateAppointment(UpdateAppointmentPatientRequest updateAppointmentPatientRequest, Long userId, Long appointmentId) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        if (!Objects.equals(appointmentFromDb.getPatient().getId(), userId)) {
            throw new ApiAuthorizationException("Patient id does not match user id");
        }
        LocalDateTime newStartedAt = updateAppointmentPatientRequest.startedAt();
        LocalDateTime newFinishedAt = newStartedAt.plusMinutes(appointmentFromDb.getService().getDuration());
        appointmentValidator.validateTimeSlot(newStartedAt, newFinishedAt, appointmentFromDb.getDoctor().getId(),
                appointmentFromDb.getService().getId());

        appointmentFromDb.setStartedAt(newStartedAt);
        appointmentFromDb.setFinishedAt(newFinishedAt);
        appointmentFromDb.setDescription(updateAppointmentPatientRequest.description());

        applicationEventPublisher.publishEvent(new AppointmentRescheduledEvent(appointmentFromDb.getPatient(),
                appointmentFromDb));
    }

    @Override
    @Transactional
    public void deleteAppointment(Long appointmentId, Long userId) {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        if (!Objects.equals(appointmentFromDb.getPatient().getId(), userId)) {
            throw new ApiAuthorizationException("Patient id does not match user id");
        }
        appointmentRepository.delete(appointmentFromDb);
        applicationEventPublisher.publishEvent(
                new AppointmentDeletedEvent(appointmentFromDb.getPatient(), appointmentFromDb));
    }

    @Override
    public GetAppointmentPatientResponse getAppointmentForPatient(Long appointmentId, Long userId) throws IOException {
        Appointment appointmentFromDb = appointmentRepository.findById(appointmentId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(appointmentFromDb.getPatient().getId(), userId)) {
            throw new ApiAuthorizationException("user id does not match patient id");
        }
        return GetAppointmentPatientResponse.builder()
                .doctorName(appointmentFromDb.getDoctor().getName())
                .doctorSurname(appointmentFromDb.getDoctor().getName())
                .doctorImage(objectStorageService.generateSignedUrl(appointmentFromDb.getDoctor().getProfilePicture()))
                .serviceName(appointmentFromDb.getService().getName())
                .startedAt(appointmentFromDb.getStartedAt())
                .price(appointmentFromDb.getService().getPrice())
                .paymentAmount(appointmentFromDb.getPayment() != null ?
                        appointmentFromDb.getPayment().getAmount() : null)
                .build();
    }

}
