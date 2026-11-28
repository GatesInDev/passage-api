package br.com.passage.api.fleet.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Ônibus de uma empresa, com a configuração física dos seus assentos.
 *
 * <p>A capacidade não é armazenada: é a quantidade de assentos ativos, o que evita
 * divergência entre o valor declarado e os assentos cadastrados.</p>
 *
 * @author Vitor Hugo Altmann <contato@vitoraltmann.dev>
 */
@Entity
@Table(name = "bus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Bus extends EntityBase {

    /** Empresa dona do ônibus. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Company company;

    /** Código interno do ônibus na operação da empresa. */
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "model", nullable = false, length = 128)
    private String model;

    @Column(name = "manufacturer", nullable = false, length = 128)
    private String manufacturer;

    /** Placa, única no sistema. */
    @Column(name = "license_plate", nullable = false, length = 8, unique = true)
    private String licensePlate;

    /** Assentos configurados. */
    @OneToMany(
            mappedBy = "bus",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    private List<Seat> seats = new ArrayList<>();

    /** Quantidade de assentos ativos e não excluídos. */
    public int getCapacity() {
        return (int) seats.stream()
                .filter(seat -> seat.isActive() && !seat.isDeleted())
                .count();
    }
}