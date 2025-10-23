package pl.edu.pja.aurorumclinic.features.specializations.queries.shared;

import lombok.Builder;

@Builder
public record GetSpecializationResponse(Long id,
                                        String name) {
}
