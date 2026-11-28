package br.com.passage.api.integration.internal.domain.entities;

import br.com.passage.api.integration.internal.domain.enums.FiscalDocumentStatus;
import br.com.passage.api.integration.internal.domain.enums.FiscalEnvironment;
import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Bilhete de Passagem Eletrônico (BP-e, modelo 63) de uma passagem vendida.
 *
 * <p>Nasce {@code PENDING} e vai para {@code AUTHORIZED} ou {@code REJECTED}. Depois de autorizado,
 * só pode ser {@code CANCELED}: documento fiscal não se apaga. A comunicação com a SEFAZ é simulada
 * (ver {@link br.com.passage.api.integration.internal.gateway.MockSefazGateway}).</p>
 */
@Entity
@Table(
        name = "fiscal_documents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fiscal_document_access_key", columnNames = "access_key"),
                @UniqueConstraint(name = "uk_fiscal_document_ticket", columnNames = "ticket_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FiscalDocument extends EntityBase {

    /** Chave de acesso de 44 dígitos; nula até a autorização. */
    @Column(name = "access_key", length = 44)
    private String accessKey;

    /** Modelo do documento; o BP-e é sempre o 63. */
    @Column(name = "model", nullable = false, length = 2)
    private String model;

    @Column(name = "series", nullable = false)
    private Integer series;

    @Column(name = "number", nullable = false)
    private Long number;

    /** Protocolo devolvido pela SEFAZ na autorização. */
    @Column(name = "protocol", length = 20)
    private String protocol;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FiscalDocumentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false, length = 20)
    private FiscalEnvironment environment;

    /** Passagem que originou o documento; um documento por passagem. */
    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    /** Empresa emitente, herdada da passagem. */
    @Column(name = "company_uuid", nullable = false)
    private UUID companyUuid;

    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    @Column(name = "passenger_document", nullable = false, length = 11)
    private String passengerDocument;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    /** Motivo da recusa da SEFAZ. */
    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    /** Registra a autorização da SEFAZ. */
    public void authorize(String accessKey, String protocol, Instant authorizedAt) {
        this.accessKey = accessKey;
        this.protocol = protocol;
        this.authorizedAt = authorizedAt;
        this.rejectionReason = null;
        this.status = FiscalDocumentStatus.AUTHORIZED;
    }

    /**
     * Registra a recusa da SEFAZ.
     */
    public void reject(String reason) {
        this.rejectionReason = reason;
        this.status = FiscalDocumentStatus.REJECTED;
    }

    /**
     * Cancela o documento.
     *
     * @throws IllegalStateException se o documento não estiver autorizado
     */
    public void cancel(Instant canceledAt) {
        if (this.status != FiscalDocumentStatus.AUTHORIZED) {
            throw new IllegalStateException("Apenas documentos autorizados podem ser cancelados.");
        }
        this.canceledAt = canceledAt;
        this.status = FiscalDocumentStatus.CANCELED;
    }

    public boolean isAuthorized() {
        return this.status == FiscalDocumentStatus.AUTHORIZED;
    }
}
