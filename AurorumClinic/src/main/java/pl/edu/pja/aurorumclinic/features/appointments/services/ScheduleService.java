package pl.edu.pja.aurorumclinic.features.appointments.services;

import pl.edu.pja.aurorumclinic.features.appointments.dtos.CreateScheduleRequest;

public interface ScheduleService {
    void createSchedule(CreateScheduleRequest createScheduleRequest);
}
