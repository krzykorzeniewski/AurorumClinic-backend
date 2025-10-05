package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.pja.aurorumclinic.features.users.dtos.response.*;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.request.PutPatientRequest;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiException;
import pl.edu.pja.aurorumclinic.shared.data.models.Patient;
import pl.edu.pja.aurorumclinic.shared.exceptions.ApiNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public List<GetPatientResponse> getAllPatients(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patientsFromDb;
        if (query == null) {
            patientsFromDb = patientRepository.findAll(pageable);
        } else {
            patientsFromDb = patientRepository.searchAllByQuery(query, pageable);
        }
        List<GetPatientResponse> patientDtos = new ArrayList<>();
        for (Patient patient : patientsFromDb) {
            GetPatientResponse patientDto = mapPatientToGetResponseDto(patient);
            patientDtos.add(patientDto);
        }
        return patientDtos;
    }

    @Override
    public GetPatientResponse getPatientById(Long patientId) {
        Patient patientFromDb = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiNotFoundException("Id not found", "id"));
        GetPatientResponse patientDto = mapPatientToGetResponseDto(patientFromDb);
        return patientDto;
    }

    @Override
    @Transactional
    public GetPatientResponse partiallyUpdatePatient(Long patientId, PatchPatientRequest requestDto) {
        Patient patientFromDb = patientRepository.findById(patientId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!patientFromDb.isPhoneNumberVerified() &&
                Objects.equals(requestDto.communicationPreferences(), CommunicationPreference.PHONE_NUMBER)) {
            throw new ApiException("phone number is not verified", "communicationPreferences");
        }
        patientFromDb.setCommunicationPreferences(requestDto.communicationPreferences());
        patientFromDb.setNewsletter(requestDto.newsletter());

        GetPatientResponse responseDto = mapPatientToGetResponseDto(patientRepository.save(patientFromDb));
        return responseDto;
    }

    @Override
    @Transactional
    public GetPatientResponse updatePatient(Long patientId, PutPatientRequest requestDto) {
        Patient patientFromDb = patientRepository.findById(patientId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (patientRepository.findByEmail(requestDto.email()) != null) {
            throw new ApiException("Email already in use", "email");
        }
        patientFromDb.setName(requestDto.name());
        patientFromDb.setSurname(requestDto.surname());
        patientFromDb.setPesel(requestDto.pesel());
        patientFromDb.setBirthdate(requestDto.birthdate());
        patientFromDb.setPhoneNumber(requestDto.phoneNumber());
        patientFromDb.setEmail(requestDto.email());
        patientFromDb.setCommunicationPreferences(requestDto.communicationPreferences());
        patientFromDb.setNewsletter(requestDto.newsletter());
        patientFromDb.setTwoFactorAuth(requestDto.twoFactorAuth());

        GetPatientResponse responseDto = mapPatientToGetResponseDto(patientRepository.save(patientFromDb));
        return responseDto;
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        Patient patientFromDb = patientRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        patientRepository.delete(patientFromDb);
    }

    @Override
    public Page<GetPatientAppointmentResponse> getPatientAppointments(Long patientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GetPatientAppointmentResponse> patientsFromDb = patientRepository
                .findPatientAppointmentsById(patientId, pageable);
        return patientsFromDb;
    }

    private GetPatientResponse mapPatientToGetResponseDto(Patient patient) {
        return GetPatientResponse.builder()
                .id(patient.getId())
                .name(patient.getName())
                .surname(patient.getSurname())
                .pesel(patient.getPesel())
                .birthDate(patient.getBirthdate())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .twoFactorAuth(patient.isTwoFactorAuth())
                .newsletter(patient.isNewsletter())
                .communicationPreferences(patient.getCommunicationPreferences().name())
                .emailVerified(patient.isEmailVerified())
                .phoneNumberVerified(patient.isPhoneNumberVerified())
                .build();
    }
}
