package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Newsletter", columnDefinition = "bit")
    private boolean newsletter;

    @Column(name = "Communication_Preferences", columnDefinition = "bit")
    private boolean communicationPreferences;

    @OneToMany(mappedBy = "patient")
    private List<Appointment> appointments;

    @JoinColumn(name = "PK_Patient")
    @OneToOne
    @MapsId
    private User user;

}
