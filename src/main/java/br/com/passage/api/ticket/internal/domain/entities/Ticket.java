package br.com.passage.api.ticket.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Passagem vendida para um assento de uma viagem.
 *
 * <p>Preço, partida e empresa são copiados da viagem na venda.</p>
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Ticket extends EntityBase {

    /** Código da passagem, único no sistema. */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    /** CPF do passageiro, sem pontuação. */
    @Column(name = "passenger_document", nullable = false, length = 11)
    private String passengerDocument;

    /** Assento vendido, do ônibus da viagem. */
    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    /** Preço pago, em reais. */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Partida da viagem no momento da venda. */
    @Column(name = "departure_date_time", nullable = false)
    private Instant departureDateTime;

    /** Empresa dona da passagem, a mesma da viagem. */
    @Column(name = "company_uuid", nullable = false)
    private UUID companyUuid;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;
}
