package pl.edu.pja.aurorumclinic.features.appointments.employees;


public interface AppointmentEmployeeService {

    void createAppointment(CreateAppointmentEmployeeRequest request);
    void updateAppointment(Long appointmentId, UpdateAppointmentEmployeeRequest request);

    void deleteAppointment(Long appointmentId);
}
