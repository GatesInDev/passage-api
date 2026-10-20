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

    /**
     * Indica se a rota pertence à empresa informada.
     *
     * <p>Utilizado para restringir alterações às rotas da própria empresa
     * do usuário. Retorna {@code false} quando o identificador informado é
     * nulo, como no caso de usuários sem empresa vinculada.</p>
     *
     * @param companyUuid UUID da empresa a ser verificada
     * @return {@code true} se a rota pertence à empresa informada
     */
    public boolean belongsTo(UUID companyUuid){
        if (companyUuid == null){
            return false;
        }
        return companyUuid.equals(this.companyUuid);
    }
}
