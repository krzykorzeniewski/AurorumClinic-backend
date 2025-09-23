package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.CommunicationPreference;

import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@PrimaryKeyJoinColumn(name = "PK_User")
public class Patient extends User{

    @Column(name = "Newsletter", columnDefinition = "bit")
    private boolean newsletter;

    @Column(name = "Communication_Preferences", length = 50, columnDefinition = "nvarchar(50)")
    @Enumerated(value = EnumType.STRING)
    @NotNull
    private CommunicationPreference communicationPreferences;

    @OneToMany(mappedBy = "patient", orphanRemoval = true)
    private List<Appointment> appointments;

}
