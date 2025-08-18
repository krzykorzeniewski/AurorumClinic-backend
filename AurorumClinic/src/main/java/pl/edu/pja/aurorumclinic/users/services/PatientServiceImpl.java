package pl.edu.pja.aurorumclinic.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.users.PatientRepository;
import pl.edu.pja.aurorumclinic.users.dtos.response.GetPatientResponse;
import pl.edu.pja.aurorumclinic.users.dtos.request.PatchPatientRequest;
import pl.edu.pja.aurorumclinic.users.dtos.request.PutPatientRequest;
import pl.edu.pja.aurorumclinic.users.shared.EmailNotUniqueException;
import pl.edu.pja.aurorumclinic.users.shared.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService{

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
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id: " + patientId + " does not exist"));
        GetPatientResponse patientDto = mapPatientToGetResponseDto(patientFromDb);
        return patientDto;
    }

    @Override
    public GetPatientResponse partiallyUpdatePatient(Long patientId, PatchPatientRequest requestDto) {
        Patient patientFromDb = patientRepository.findById(patientId).orElseThrow(
                () -> new ResourceNotFoundException("Patient with id: " + patientId + " does not exist")
        );
        if (patientRepository.findByEmail(requestDto.email()) != patientFromDb) {
            throw new EmailNotUniqueException("Email already taken: " + requestDto.email());
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
                () -> new ResourceNotFoundException("Patient with id: " + patientId + " does not exist")
        );
        if (patientRepository.findByEmail(requestDto.email()) != null) {
            throw new EmailNotUniqueException("Email already taken: " + requestDto.email());
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
