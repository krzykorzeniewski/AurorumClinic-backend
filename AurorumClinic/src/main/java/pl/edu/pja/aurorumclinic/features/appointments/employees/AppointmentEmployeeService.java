package pl.edu.pja.aurorumclinic.features.appointments.employees;


public interface AppointmentEmployeeService {

    void createAppointment(CreateAppointmentEmployeeRequest request);
    void updateAppointment(UpdateAppointmentEmployeeRequest request);
}
