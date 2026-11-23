package br.com.passage.api.trip.internal.domain.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.shared.domain.entities.EntityBase;
import br.com.passage.api.trip.internal.domain.enums.TripStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
     * <p>Representa o UUID da rota percorrida. A rota pertence a outro
     * módulo, por isso é referenciada apenas pelo identificador.</p>
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
     * Indica se a viagem pertence à empresa informada.
     *
     * <p>Utilizado para restringir alterações às viagens da própria empresa
     * do usuário. Retorna {@code false} quando o identificador informado é
     * nulo, como no caso de usuários sem empresa vinculada.</p>
     *
     * @param companyUuid UUID da empresa a ser verificada
     * @return {@code true} se a viagem pertence à empresa informada
     */
    public boolean belongsTo(UUID companyUuid) {
        if (companyUuid == null) {
            return false;
        }
        return companyUuid.equals(this.companyUuid);
    }
}
