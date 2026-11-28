package br.com.passage.api.fleet.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;

/** Motorista de uma empresa. */
@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Driver extends EntityBase {

    @Column(nullable = false, length = 100)
    private String name;

    /** Número da CNH, único no sistema. */
    @Column(name = "license_number", nullable = false, unique = true, length = 20)
    private String licenseNumber;

    /** Categoria da CNH (ex.: D, E). */
    @Column(name = "license_category", length = 5)
    private String licenseCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}