package pl.edu.pja.aurorumclinic.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.pja.aurorumclinic.models.Patient;
import pl.edu.pja.aurorumclinic.users.PatientRepository;
import pl.edu.pja.aurorumclinic.users.dtos.GetPatientResponseDto;
import pl.edu.pja.aurorumclinic.users.dtos.PatchPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.dtos.PutPatientRequestDto;
import pl.edu.pja.aurorumclinic.users.shared.EmailNotUniqueException;
import pl.edu.pja.aurorumclinic.users.shared.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService{

    private final PatientRepository patientRepository;

    @Override
    public List<GetPatientResponseDto> getAllPatients() {
        List<Patient> patientsFromDb = patientRepository.findAll();
        List<GetPatientResponseDto> patientDtos = new ArrayList<>();
        for (Patient patient : patientsFromDb) {
            GetPatientResponseDto patientDto = mapPatientToGetResponseDto(patient);
            patientDtos.add(patientDto);
        }
        return patientDtos;
    }

    @Override
    public GetPatientResponseDto getPatientById(Long patientId) {
        Patient patientFromDb = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient with id: " + patientId + " does not exist"));
        GetPatientResponseDto patientDto = mapPatientToGetResponseDto(patientFromDb);
        return patientDto;
    }

    @Override
    public GetPatientResponseDto partiallyUpdatePatient(Long patientId, PatchPatientRequestDto requestDto) {
        Patient patientFromDb = patientRepository.findById(patientId).orElseThrow(
                () -> new ResourceNotFoundException("Patient with id: " + patientId + " does not exist")
        );
        if (patientRepository.findByEmail(requestDto.email()) != null) {
            throw new EmailNotUniqueException("Email already taken: " + requestDto.email());
        }
        patientFromDb.setPhoneNumber(requestDto.phoneNumber());
        patientFromDb.setEmail(requestDto.email());
        patientFromDb.setCommunicationPreferences(requestDto.communicationPreferences());
        patientFromDb.setNewsletter(requestDto.newsletter());
        patientFromDb.setTwoFactorAuth(requestDto.twoFactorAuth());

        GetPatientResponseDto responseDto = mapPatientToGetResponseDto(patientRepository.save(patientFromDb));
        return responseDto;
    }

    @Override
    public GetPatientResponseDto updatePatient(Long patientId, PutPatientRequestDto requestDto) {
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

        GetPatientResponseDto responseDto = mapPatientToGetResponseDto(patientRepository.save(patientFromDb));
        return responseDto;
    }

    private GetPatientResponseDto mapPatientToGetResponseDto(Patient patient) {
        return GetPatientResponseDto.builder()
                .id(patient.getId())
                .name(patient.getName())
                .surname(patient.getSurname())
                .pesel(patient.getPesel())
                .birthDate(patient.getBirthdate())
                .email(patient.getEmail())
                .twoFactorAuth(patient.isTwoFactorAuth())
                .newsletter(patient.isNewsletter())
                .communicationPreferences(patient.getCommunicationPreferences().name())
                .build();
    }
}
