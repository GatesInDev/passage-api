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
 * Representa uma viagem agendada de uma rota.
 *
 * <p>Enquanto a rota descreve o trajeto fixo de uma linha, a viagem é uma
 * saída específica desse trajeto, em uma data e horário definidos, operada
 * por um ônibus e um motorista.</p>
 *
 * <p>Uma rota pode possuir várias viagens. A viagem pertence à mesma
 * empresa da rota.</p>
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

    /**
     * Código da viagem.
     *
     * <p>Identificador único da viagem, utilizado para referência e identificação.</p>
     */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    /**
     * Identificador da rota da viagem.
     *
     * <p>Representa o UUID da rota percorrida. A rota pertence ao
     * módulo de rotas e é referenciada apenas pelo identificador.</p>
     */
    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    /**
     * Identificador da empresa responsável pela viagem.
     *
     * <p>Representa o UUID da empresa que opera a viagem. Corresponde à
     * empresa da rota associada.</p>
     */
    @Column(name = "company_uuid", nullable = false)
    private UUID companyUuid;

    /**
     * Identificador do ônibus da viagem.
     *
     * <p>Representa o UUID do ônibus alocado para realizar a viagem.</p>
     */
    @Column(name = "bus_id", nullable = false)
    private UUID busId;

    /**
     * Identificador do motorista da viagem.
     *
     * <p>Representa o UUID do motorista responsável por conduzir a viagem.</p>
     */
    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    /**
     * Data e hora de partida da viagem.
     *
     * <p>Representa o momento previsto para a saída do ônibus, em UTC.</p>
     */
    @Column(name = "departure_time", nullable = false)
    private Instant departureTime;

    /**
     * Data e hora de chegada prevista da viagem.
     *
     * <p>Representa o momento previsto para a chegada ao destino, em UTC.
     * Deve ser posterior à data de partida.</p>
     */
    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;

    /**
     * Situação atual da viagem.
     *
     * <p>Indica em qual etapa do ciclo de vida a viagem se encontra, como
     * agendada, em embarque, em andamento, concluída ou cancelada.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private TripStatus status;

    /**
     * Preço base da passagem da viagem.
     *
     * <p>Representa o valor de referência da passagem, utilizado na venda
     * dos bilhetes.</p>
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Altera a situação da viagem, validando se a transição é permitida.
     *
     * <p>É a única forma de alterar a situação depois do cadastro. As
     * transições válidas são definidas por {@link TripStatus#canTransitionTo}.</p>
     *
     * @param next nova situação da viagem
     * @throws BusinessException se a transição não for permitida
     */
    public void changeStatus(TripStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new BusinessException("Não é possível alterar a situação da viagem de " + this.status + " para " + next + ".");
        }
        this.status = next;
    }

    /**
     * Verifica se a viagem ainda pode ser alterada.
     *
     * <p>Viagens nas situações {@link TripStatus#COMPLETED} e {@link TripStatus#CANCELED} são
     * finais e não aceitam alterações.</p>
     *
     * @throws BusinessException se a viagem estiver concluída ou cancelada
     */
    public void ensureEditable() {
        if (this.status.isFinal()){
            throw new BusinessException("Não é possível alterar uma viagem que já foi concluída ou cancelada.");
        }
    }
}
