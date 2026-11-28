
package br.com.passage.api.fleet.internal.domain.entities;

import br.com.passage.api.fleet.internal.domain.enums.SeatPosition;
import br.com.passage.api.fleet.internal.domain.enums.SeatQuality;
import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Representa um assento físico pertencente a um {@link Bus}.
 *
 * <p>O assento possui informações suficientes para representar sua
 * posição física dentro do ônibus e suas características para utilização
 * pelos passageiros.</p>
 *
 * <h2>Disposição física</h2>
 *
 * <p>A posição é determinada por:</p>
 *
 * <ul>
 *     <li>{@link #row} — fileira do assento;</li>
 *     <li>{@link #column} — coluna do assento;</li>
 *     <li>{@link #seatNumber} — identificação apresentada ao passageiro.</li>
 * </ul>
 *
 * <h2>Características</h2>
 *
 * <ul>
 *     <li>{@link #position} — posição relativa no corredor;</li>
 *     <li>{@link #quality} — categoria de qualidade do assento;</li>
 *     <li>{@link #isAccessible} — indica se o assento é destinado
 *         à acessibilidade.</li>
 * </ul>
 *
 * <h2>Posição</h2>
 *
 * <p>Um assento pode estar localizado junto à janela, junto ao corredor
 * ou entre outros assentos.</p>
 *
 * <h2>Qualidade</h2>
 *
 * <p>A qualidade permite que diferentes categorias de assentos sejam
 * utilizadas na precificação ou na escolha do passageiro.</p>
 *
 * @see Bus
 * @see EntityBase
 */
@Entity
@Table(
        name = "seat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_bus_number",
                        columnNames = {"bus_id", "seat_number"}
                ),
                @UniqueConstraint(
                        name = "uk_seat_bus_position",
                        columnNames = {"bus_id", "row_number", "column_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Seat extends EntityBase {

    /**
     * Ônibus ao qual o assento pertence.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    @ToString.Exclude
    private Bus bus;

    /**
     * Número do assento apresentado ao passageiro.
     *
     * <p>Exemplo: {@code 01}, {@code 02}, {@code 15}.</p>
     */
    @Column(name = "seat_number", nullable = false, length = 8)
    private String seatNumber;

    /**
     * Número da fileira na disposição física do ônibus.
     *
     * <p>A numeração normalmente começa em 1.</p>
     */
    @Column(name = "row_number", nullable = false)
    private Integer row;

    /**
     * Número da coluna na disposição física do ônibus.
     *
     * <p>A numeração normalmente começa em 1 e representa a posição
     * horizontal do assento dentro da fileira.</p>
     */
    @Column(name = "column_number", nullable = false)
    private Integer column;

    /**
     * Posição do assento em relação ao corredor e à janela.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 16)
    private SeatPosition position;

    /**
     * Categoria de qualidade do assento.
     *
     * <p>Pode ser utilizada para diferenciar assentos comuns,
     * confortáveis ou premium, inclusive para fins de precificação.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "quality", nullable = false, length = 16)
    private SeatQuality quality;

    /**
     * Indica se o assento é destinado à acessibilidade.
     */
    @Column(name = "is_accessible", nullable = false)
    private boolean isAccessible;
}