package pl.edu.pja.aurorumclinic.features.appointments.specializations.queries.shared;

import lombok.Builder;

@Builder
public record GetSpecializationResponse(Long id,
                                        String name) {
}
