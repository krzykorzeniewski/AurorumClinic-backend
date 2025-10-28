package pl.edu.pja.aurorumclinic.features.statistics;

public record GetAppointmentStatsResponse(Long scheduled,
                                          Long finished,
                                          Double avgDuration,
                                          Double avgRating) {
}
