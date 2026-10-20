package br.com.passage.api.ticket.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
     * <p>Referenciado por UUID para manter o desacoplamento entre módulos.</p>
     *
     * <p>A classe do serviço da viagem não é armazenada aqui: ela é determinada
     * pela qualidade do assento reservado, evitando manter o mesmo dado em dois
     * lugares com risco de divergência.</p>
     */
    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    /**
     * Valor da passagem em reais.
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Data e hora de partida da viagem.
     */
    @Column(name = "departure_date_time", nullable = false)
    private Instant departureDateTime;

    /**
     * Identificador da rota associada à passagem.
     *
     * <p>Referenciado por UUID para manter o desacoplamento entre módulos.</p>
     */
    @Column(name = "route_id", nullable = false)
    private UUID routeId;
}
