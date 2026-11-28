package br.com.passage.api.trip.internal.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.shared.domain.entities.EntityBase;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.trip.enums.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Saída de uma rota em data e horário definidos, com ônibus e motorista alocados.
 *
 * <p>A viagem herda a empresa da rota. Rota, ônibus e motorista pertencem a outros
 * módulos e são referenciados apenas pelo identificador.</p>
 */
@Entity
@Table(name = "trip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Trip extends EntityBase {

    /** Código único, imutável após o cadastro. */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    /** Rota percorrida, imutável após o cadastro. */
    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    /** Empresa operadora, copiada da rota no cadastro. */
    @Column(name = "company_uuid", nullable = false)
    private UUID companyUuid;

    /** Ônibus alocado, da mesma empresa e sem outra viagem no período. */
    @Column(name = "bus_id", nullable = false)
    private UUID busId;

    /** Motorista alocado, da mesma empresa e sem outra viagem no período. */
    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    /** Partida prevista (UTC). */
    @Column(name = "departure_time", nullable = false)
    private Instant departureTime;

    /** Chegada prevista (UTC), sempre posterior à partida. */
    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;

    /** Situação atual; alterada apenas por {@link #changeStatus}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private TripStatus status;

    /** Preço base da passagem, usado na venda. */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Altera a situação, respeitando as transições de {@link TripStatus#canTransitionTo}.
     *
     * @throws BusinessException se a transição não for permitida
     */
    public void changeStatus(TripStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new BusinessException("Não é possível alterar a situação da viagem de " + this.status + " para " + next + ".");
        }
        this.status = next;
    }

    /** @throws BusinessException se a viagem estiver concluída ou cancelada */
    public void ensureEditable() {
        if (this.status.isFinal()){
            throw new BusinessException("Não é possível alterar uma viagem que já foi concluída ou cancelada.");
        }
    }
}
