package pl.edu.pja.aurorumclinic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Message")
    private Long id;

    @Column(name = "Message")
    @Size(max = 500)
    @NotBlank
    private String message;

    @Column(name = "Response")
    @Size(max = 500)
    private String response;

    @Column(name = "Sent_At", columnDefinition = "datetime2(2)")
    @NotNull
    private LocalDateTime sentAt;

    @ManyToOne
    @JoinColumn(name = "FK_Doctor")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "FK_Patient")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "FK_Chatroom")
    private Chatroom chatroom;
}
