package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import pl.edu.pja.aurorumclinic.models.enums.CommunicationPreference;

import java.util.List;

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
    private CommunicationPreference communicationPreferences;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;

}
