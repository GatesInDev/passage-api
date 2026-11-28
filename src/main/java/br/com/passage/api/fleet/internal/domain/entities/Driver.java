package br.com.passage.api.fleet.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;

/**
 * Representa um motorista vinculado a uma empresa transportadora no módulo de Frota.
 * <p>
 * Armazena informações cadastrais e regulatórias da habilitação (CNH) do condutor,
 * herdando atributos de auditoria e identificação pública da {@link EntityBase}.
 * </p>
 */
@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Driver extends EntityBase {

    /**
     * Nome completo do motorista.
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Número de registro da Carteira Nacional de Habilitação (CNH).
     * Deve ser único no sistema.
     */
    @Column(name = "license_number", nullable = false, unique = true, length = 20)
    private String licenseNumber;

    /**
     * Categoria da CNH do motorista (ex: "D", "E").
     */
    @Column(name = "license_category", length = 5)
    private String licenseCategory;

    /**
     * Empresa transportadora à qual o motorista está vinculado.
     * Mapeamento com carregamento preguiçoso (lazy loading).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}