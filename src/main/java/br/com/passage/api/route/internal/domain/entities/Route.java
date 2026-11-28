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
 * Linha regular entre uma origem e um destino, operada por uma empresa.
 *
 * <p>A rota é o trajeto fixo; cada saída em data e horário definidos é uma viagem
 * do módulo {@code trip}. Uma rota inativa não recebe novas viagens.</p>
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

    /** Prefixo da linha no órgão regulador, único e imutável após o cadastro. */
    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    /** Empresa que opera a rota, referenciada pelo identificador por pertencer a outro módulo. */
    @Column(name = "company_uuid", nullable = false)
    private UUID companyUuid;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "origin_city", nullable = false, length = 128)
    private String originCity;

    /** UF de origem, com duas letras. */
    @Column(name = "origin_state", nullable = false, length = 2)
    private String originState;

    @Column(name = "destination_city", nullable = false, length = 128)
    private String destinationCity;

    /** UF de destino, com duas letras. */
    @Column(name = "destination_state", nullable = false, length = 2)
    private String destinationState;

    /** Distância total, em quilômetros. */
    @Column(name = "distance_km", nullable = false, precision = 7, scale = 2)
    private BigDecimal distanceKm;

    /** Duração estimada do trajeto, em minutos. */
    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes;

    /** Abrangência da linha, que determina o órgão regulador e a coerência entre as UFs. */
    @Enumerated(EnumType.STRING)
    @Column(name = "service_scope", nullable = false, length = 20)
    private RouteScope serviceScope;

    /** Órgão regulador da linha (ex.: ANTT, DETER-SC). */
    @Column(name = "regulatory_agency", nullable = false, length = 64)
    private String regulatoryAgency;

}
