package pl.edu.pja.aurorumclinic.shared.data.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentMethod;
import pl.edu.pja.aurorumclinic.shared.data.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_Payment")
    private Long id;

    @NotNull(message = "This field is required")
    @Column(name = "Amount", columnDefinition = "numeric(10,2)")
    private BigDecimal amount;

    @Column(name = "Placed_At", columnDefinition = "datetime2(2)")
    @NotNull(message = "This field is required")
    private LocalDateTime placedAt;

    @Enumerated(value = EnumType.STRING)
    @NotNull(message = "This field is required")
    @Column(name = "Method", length = 50, columnDefinition = "nvarchar(50)")
    private PaymentMethod method;

    @Enumerated(value = EnumType.STRING)
    @NotNull(message = "This field is required")
    @Column(name = "Status", length = 50, columnDefinition = "nvarchar(50)")
    private PaymentStatus status;

    @OneToOne(optional = false)
    @JoinColumn(name = "FK_appointment")
    private Appointment appointment;
}
