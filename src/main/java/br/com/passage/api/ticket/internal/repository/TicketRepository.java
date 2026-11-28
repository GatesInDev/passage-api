package br.com.passage.api.ticket.internal.repository;

import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso às passagens.
 *
 * <p>As consultas {@code Visible} desconsideram registros excluídos e, quando
 * {@code companyId} é informado, restringem o resultado a essa empresa.</p>
 */
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

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

    /** Considera também as passagens excluídas, já que o código continua reservado. */
    boolean existsByCode(String code);

    /** Indica se o assento já foi vendido para a viagem; passagens excluídas liberam o assento. */
    boolean existsByTripIdAndSeatIdAndIsDeletedFalse(UUID tripId, UUID seatId);
}
