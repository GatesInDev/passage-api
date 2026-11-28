package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de acesso aos assentos.
 */
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    /**
     * Busca um assento não excluído de um ônibus.
     *
     * @param id    identificador do assento
     * @param busId identificador do ônibus
     * @return assento encontrado, se existir
     */
    Optional<Seat> findByIdAndBus_IdAndIsDeletedFalse(UUID id, UUID busId);

    /**
     * Lista os assentos não excluídos de um ônibus.
     *
     * @param busId identificador do ônibus
     * @return assentos encontrados
     */
    List<Seat> findAllByBus_IdAndIsDeletedFalse(UUID busId);

    /**
     * Verifica se outro assento do ônibus já utiliza o número ou a posição informada.
     *
     * <p>A posição é formada pela fileira e pela coluna. Assentos excluídos logicamente também
     * são considerados.</p>
     *
     * @param busId      identificador do ônibus
     * @param seatNumber número do assento
     * @param row        fileira do assento
     * @param column     coluna do assento
     * @param excludedId assento desconsiderado na verificação, ou {@code null}
     * @return {@code true} se o número ou a posição já estiverem em uso
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
