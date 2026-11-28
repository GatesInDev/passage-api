
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
 * Assento de um ônibus, posicionado por fileira e coluna.
 *
 * <p>O número e a posição são únicos no ônibus e continuam reservados mesmo após a
 * exclusão lógica do assento.</p>
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    @ToString.Exclude
    private Bus bus;

    /** Número exibido ao passageiro (ex.: {@code 01}, {@code 15}). */
    @Column(name = "seat_number", nullable = false, length = 8)
    private String seatNumber;

    /** Fileira, a partir de 1. */
    @Column(name = "row_number", nullable = false)
    private Integer row;

    /** Coluna dentro da fileira, a partir de 1. */
    @Column(name = "column_number", nullable = false)
    private Integer column;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 16)
    private SeatPosition position;

    /** Categoria do assento, que pode diferenciar o preço. */
    @Enumerated(EnumType.STRING)
    @Column(name = "quality", nullable = false, length = 16)
    private SeatQuality quality;

    /** Se o assento é reservado à acessibilidade. */
    @Column(name = "is_accessible", nullable = false)
    private boolean isAccessible;
}