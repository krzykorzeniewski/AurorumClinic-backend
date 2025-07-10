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
public class Chatroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Chatroom")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "FK_Doctor")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "FK_Patient")
    private Patient patient;

    @OneToMany(mappedBy = "chatroom")
    private List<Message> messages;

}
