package br.com.passage.api.route.internal.domain.entities;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.passage.api.route.internal.domain.enums.RouteScope;
import br.com.passage.api.shared.domain.entities.EntityBase;
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
 * Representa uma rota atendida por uma empresa transportadora.
 *
 * <p>A rota descreve o trajeto fixo de uma linha, com origem, destino, distância, duração
 * estimada e abrangência. Cada saída da rota em uma data e horário definidos é representada
 * por uma viagem do módulo de viagens.</p>
 *
 * <h2>Abrangência</h2>
 *
 * <p>A abrangência ({@link RouteScope}) deve ser coerente com a origem e o destino:</p>
 *
 * <ul>
 *     <li>rotas estaduais e metropolitanas começam e terminam no mesmo estado;</li>
 *     <li>rotas interestaduais ligam estados diferentes;</li>
 *     <li>a origem e o destino não podem ser a mesma cidade.</li>
 * </ul>
 *
 * <h2>Situação</h2>
 *
 * <p>Uma rota inativa permanece disponível para consulta, mas não recebe novas viagens.</p>
 *
 * @see RouteScope
 * @see EntityBase
 */
@Entity
@Table(name = "route")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Route extends EntityBase {

    /**
     * Código da rota.
     *
     * <p>Identificador único da rota, utilizado para referência e identificação.</p>
     */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    /**
     * Identificador da empresa associada à rota.
     *
     * <p>Representa o UUID da empresa que possui ou opera a rota.</p>
     */
    @Column(name = "company_uuid", nullable = false)
    private UUID companyUuid;

    /**
     * Nome da rota.
     *
     * <p>Nome descritivo da rota, utilizado para referência e identificação.</p>
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * Cidade de origem da rota.
     *
     * <p>Indica a cidade de onde a rota se inicia.</p>
     */
    @Column(name = "origin_city", nullable = false, length = 128)
    private String originCity;

    /**
     * Estado de origem da rota.
     *
     * <p>Indica o estado de onde a rota se inicia.</p>
     */
    @Column(name = "origin_state", nullable = false, length = 2)
    private String originState;

    /**
     * Cidade de destino da rota.
     *
     * <p>Indica a cidade onde a rota termina.</p>
     */
    @Column(name = "destination_city", nullable = false, length = 128)
    private String destinationCity;

    /**
     * Estado de destino da rota.
     *
     * <p>Indica o estado onde a rota termina.</p>
     */
    @Column(name = "destination_state", nullable = false, length = 2)
    private String destinationState;

    /**
     * Distância da rota em quilômetros.
     *
     * <p>Representa a distância total da rota, medida em quilômetros.</p>
     */
    @Column(name = "distance_km", nullable = false, precision = 7, scale = 2)
    private BigDecimal distanceKm;

    /**
     * Duração estimada da rota em minutos.
     *
     * <p>Representa o tempo estimado para percorrer a rota, medido em minutos.</p>
     */
    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes;

    /**
     * Escopo de serviço da rota.
     *
     * <p>Indica o tipo de serviço que a rota oferece, como urbano, interestadual, etc.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_scope", nullable = false, length = 20)
    private RouteScope serviceScope;

    /**
     * Órgão regulador da linha (ex.: ANTT, DETER-SC).
     *
     * <p>Representa o órgão responsável por regulamentar a linha.</p>
     */
    @Column(name = "regulatory_agency", nullable = false, length = 64)
    private String regulatoryAgency;

}
