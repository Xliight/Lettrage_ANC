package lettrage.example.lettrage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "compte_client")
    private String compteClient;

    @Column(name = "profil_validation")
    private String profilValidation;

    @Column(name = "montant", precision = 15, scale = 4)
    private BigDecimal montant;

    @Column(name = "montant_regle", precision = 15, scale = 4)
    private BigDecimal montantRegle;

    @Column(name = "devise")
    private String devise;

    @Column(name = "settlement")
    private Long settlement;

    @Column(name = "numero_document")
    private String numeroDocument;

    @Column(name = "ordre_paiement")
    private String ordrePaiement;

    @Column(name = "date_document")
    private LocalDateTime dateDocument;

    @Column(name = "documentnum")
    private String documentNum;

    @Column(name = "date_echeance")
    private LocalDateTime dateEcheance;

    @Column(name = "facture")
    private String facture;

    @Column(name = "date_transaction")
    private LocalDateTime dateTransaction;

    @Column(name = "date_dernier_reglement")
    private LocalDateTime dateDernierReglement;

    @Column(name = "lastsettlevoucher")
    private String lastSettleVoucher;

    @Column(name = "numero_lettrage")
    private String numeroLettrage;

}
