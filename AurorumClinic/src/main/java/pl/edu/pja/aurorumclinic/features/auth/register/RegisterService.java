package pl.edu.pja.aurorumclinic.features.auth.register;

import pl.edu.pja.aurorumclinic.features.auth.register.dtos.*;

public interface RegisterService {

    void registerDoctor(RegisterDoctorRequest registerDoctorRequest);
    void registerPatient(RegisterPatientRequest registerPatientRequest);
    void registerEmployee(RegisterEmployeeRequest registerEmployeeRequest);
    void createVerifyEmailToken(VerifyEmailTokenRequest verifyEmailTokenRequest);
    void verifyEmail(VerifyEmailRequest verifyEmailRequest);
}
