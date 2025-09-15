package pl.edu.pja.aurorumclinic.features.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.features.users.dtos.GetPatientResponse;
import pl.edu.pja.aurorumclinic.features.users.dtos.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.features.users.dtos.PutPatientRequest;
import pl.edu.pja.aurorumclinic.shared.data.PatientRepository;
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
    public List<GetPatientResponse> getAllPatients() {
        List<Patient> patientsFromDb = patientRepository.findAll();
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
    public GetPatientResponse partiallyUpdatePatient(Long patientId, PatchPatientRequest requestDto) {
        Patient patientFromDb = patientRepository.findById(patientId).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (patientRepository.findByEmail(requestDto.email()) != patientFromDb) {
            throw new ApiException("Email already in use", "email");
        }
        patientFromDb.setPhoneNumber(requestDto.phoneNumber());
        patientFromDb.setEmail(requestDto.email());
        patientFromDb.setCommunicationPreferences(requestDto.communicationPreferences());
        patientFromDb.setNewsletter(requestDto.newsletter());
        patientFromDb.setTwoFactorAuth(requestDto.twoFactorAuth());

        GetPatientResponse responseDto = mapPatientToGetResponseDto(patientRepository.save(patientFromDb));
        return responseDto;
    }

    @Override
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
    public void deletePatient(Long id, Authentication authentication) {
        Patient patientFromDb = patientRepository.findById(id).orElseThrow(
                () -> new ApiNotFoundException("Id not found", "id")
        );
        if (!Objects.equals(authentication.getPrincipal(), patientFromDb.getEmail())) {
            throw new ApiException("Id does not correspond to the user's id", "id");
        }
        patientRepository.delete(patientFromDb);
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
                .build();
    }
}
