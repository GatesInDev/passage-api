package br.com.passage.api.fleet.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.ArrayList;
import java.util.List;

/**
 * Empresa transportadora, dona da frota e das rotas.
 *
 * @author Vitor Hugo Altmann <contato@vitoraltmann.dev>
 */
@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Company extends EntityBase {

    /** Razão social. */
    @Column(name = "corporate_name", nullable = false, length = 255)
    private String corporateName;

    /** Nome fantasia, opcional. */
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    /** CNPJ (inclusive alfanumérico), sem pontuação e único no sistema. */
    @CNPJ(format = CNPJ.Format.ALPHANUMERIC)
    @Column(name = "document_id", nullable = false, length = 14, unique = true)
    private String documentId;

    /** Inscrição estadual, ou {@code ISENTO}. */
    @Column(name = "state_registration", nullable = false, length = 15)
    private String stateRegistration;

    /** Órgão regulador estadual (ex.: DETER-SC, ARTESP). */
    @Column(name = "state_regulatory_agency", nullable = false, length = 255)
    private String stateRegulatoryAgency;

    /** Motoristas da empresa. */
    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Driver> drivers = new ArrayList<>();

    /** Vincula o motorista, mantendo os dois lados da relação sincronizados. */
    public void addDriver(Driver driver) {
        drivers.add(driver);
        driver.setCompany(this);
    }

    /** Desvincula o motorista, mantendo os dois lados da relação sincronizados. */
    public void removeDriver(Driver driver) {
        drivers.remove(driver);
        driver.setCompany(null);
    }
}
