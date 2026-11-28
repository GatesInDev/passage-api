package br.com.passage.api.ticket.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa uma passagem vendida para um assento de uma viagem.
 *
 * <p>A passagem registra os dados do passageiro e o assento ocupado. O preço, a data de
 * partida e a empresa são copiados da viagem no momento da venda.</p>
 *
 * <h2>Assento</h2>
 *
 * <p>Um assento só pode ser vendido uma vez por viagem. Passagens canceladas liberam o
 * assento novamente.</p>
 *
 * <h2>Relacionamentos</h2>
 *
 * <p>A viagem e o assento pertencem a outros módulos e são referenciados apenas pelos
 * identificadores.</p>
 *
 * @see EntityBase
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

    /**
     * Código único da passagem.
     *
     * <p>Identificador utilizado para referência externa da passagem.</p>
     */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    /**
     * Nome completo do passageiro.
     */
    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    /**
     * CPF do passageiro.
     */
    @Column(name = "passenger_document", nullable = false, length = 11)
    private String passengerDocument;

    /**
     * Identificador do assento reservado para a passagem.
     *
     */
    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    /**
     * Valor da passagem em reais.
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Data e hora de partida registradas na venda.
     */
    @Column(name = "departure_date_time", nullable = false)
    private Instant departureDateTime;

    /**
     * Empresa dona da passagem.
     */
    @Column(name = "company_uuid", nullable = false)
    private UUID companyUuid;

    /**
     * Identificador da viagem para a qual a passagem foi vendida.
     */
    @Column(name = "trip_id", nullable = false)
    private UUID tripId;
}
