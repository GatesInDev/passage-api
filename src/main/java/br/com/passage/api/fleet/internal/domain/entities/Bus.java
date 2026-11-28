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
 * Representa um ônibus cadastrado na plataforma.
 *
 * <p>O ônibus possui uma configuração física composta por assentos.
 * A capacidade do veículo é determinada pela quantidade de assentos
 * ativos associados ao ônibus.</p>
 *
 * <p>A disposição dos assentos é representada individualmente pela
 * entidade {@link Seat}, permitindo modelar características específicas
 * de cada posição, como localização junto à janela, corredor,
 * acessibilidade e qualidade do assento.</p>
 *
 * <h2>Configuração dos assentos</h2>
 *
 * <p>Cada assento possui uma linha ({@code row}), coluna ({@code column})
 * e número identificador ({@code seatNumber}). Essa estrutura permite
 * representar diferentes configurações de veículos, como:</p>
 *
 * <pre>
 * 01 02    03 04
 * 05 06    07 08
 * 09 10    11 12
 * </pre>
 *
 * <p>A configuração não fica limitada a um layout específico. O sistema
 * pode representar ônibus com diferentes quantidades de assentos por
 * fileira e diferentes disposições internas.</p>
 *
 * <h2>Capacidade</h2>
 *
 * <p>A capacidade não é armazenada diretamente como um campo independente.
 * Ela deve ser obtida através da quantidade de assentos ativos associados
 * ao veículo.</p>
 *
 * <h2>Relacionamento</h2>
 *
 * <p>Um ônibus possui vários assentos e cada assento pertence a exatamente
 * um ônibus.</p>
 *
 * @see EntityBase
 * @see Seat
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

    /**
     * Empresa proprietária do ônibus.
     *
     * <p>Define o escopo de acesso do veículo: o gestor de frota só enxerga os ônibus da
     * própria empresa.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Company company;

    /**
     * Identificação ou código interno do ônibus.
     *
     * <p>Permite identificar o veículo dentro da operação da empresa.</p>
     */
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    /**
     * Modelo do ônibus.
     *
     * <p>Nome ou identificação comercial do modelo do veículo.</p>
     */
    @Column(name = "model", nullable = false, length = 128)
    private String model;

    /**
     * Fabricante do ônibus.
     */
    @Column(name = "manufacturer", nullable = false, length = 128)
    private String manufacturer;

    /**
     * Placa do ônibus.
     *
     * <p>Identificador utilizado para identificação do veículo.</p>
     */
    @Column(name = "license_plate", nullable = false, length = 8, unique = true)
    private String licensePlate;

    /**
     * Assentos configurados para o ônibus.
     *
     * <p>A lista representa a disposição física dos assentos no veículo.
     * Cada assento possui informações próprias de posição e características.</p>
     */
    @OneToMany(
            mappedBy = "bus",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @ToString.Exclude
    private List<Seat> seats = new ArrayList<>();

    /**
     * Retorna a quantidade de assentos ativos disponíveis na configuração
     * física do ônibus.
     *
     * @return quantidade de assentos ativos
     */
    public int getCapacity() {
        return (int) seats.stream()
                .filter(seat -> seat.isActive() && !seat.isDeleted())
                .count();
    }
}