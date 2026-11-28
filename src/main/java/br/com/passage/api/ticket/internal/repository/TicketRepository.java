package br.com.passage.api.ticket.internal.repository;

import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Passagem visível ao usuário atual.
     *
     * <p>{@code companyId} nulo representa um administrador, que enxerga
     * as passagens de todas as empresas.</p>
     */
    @Query("""
            select item from Ticket item
            where item.id = :id and item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    Optional<Ticket> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("""
            select item from Ticket item
            where item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    List<Ticket> findAllVisible(@Param("companyId") UUID companyId);

    boolean existsByCode(String code);

    /**
     * Passagem que ocupa o assento na viagem, se houver.
     *
     * <p>Passagens canceladas liberam o assento novamente.</p>
     */
    @Query("""
            select item from Ticket item
            where item.tripId = :tripId and item.seatId = :seatId and item.isDeleted = false
            """)
    Optional<Ticket> findActiveByTripIdAndSeatId(@Param("tripId") UUID tripId, @Param("seatId") UUID seatId);
}
