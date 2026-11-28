package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Acesso aos assentos. */
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    Optional<Seat> findByIdAndBus_IdAndIsDeletedFalse(UUID id, UUID busId);

    List<Seat> findAllByBus_IdAndIsDeletedFalse(UUID busId);

    /**
     * Indica se outro assento do ônibus, mesmo excluído, já usa o número ou a posição (fileira e coluna).
     *
     * @param excludedId assento ignorado na verificação (o próprio, na atualização), ou {@code null}
     */
    @Query("""
            select count(seat) > 0 from Seat seat
            where seat.bus.id = :busId
            and (seat.seatNumber = :seatNumber or (seat.row = :row and seat.column = :column))
            and (:excludedId is null or seat.id <> :excludedId)
            """)
    boolean existsConflictingSeat(@Param("busId") UUID busId,
                                  @Param("seatNumber") String seatNumber,
                                  @Param("row") Integer row,
                                  @Param("column") Integer column,
                                  @Param("excludedId") UUID excludedId);
}
