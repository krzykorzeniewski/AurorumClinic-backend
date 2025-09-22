package pl.edu.pja.aurorumclinic.features.users.dtos.request;

import jakarta.validation.constraints.NotNull;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;

public record PatchPatientRequest(@NotNull CommunicationPreference communicationPreferences,
                                  boolean newsletter
                                  ) {
}
