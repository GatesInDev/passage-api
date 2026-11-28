package br.com.passage.api.ticket.internal.repository;

import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de acesso às passagens.
 *
 * <p>As consultas {@code Visible} desconsideram passagens excluídas. Quando {@code companyId} é
 * informado, o resultado é restrito a essa empresa; quando é {@code null}, todas as empresas
 * são consideradas.</p>
 */
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Busca uma passagem visível.
     *
     * @param id        identificador da passagem
     * @param companyId empresa à qual a busca é restrita, ou {@code null} para todas
     * @return passagem encontrada, se existir
     */
    @Query("""
            select item from Ticket item
            where item.id = :id and item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    Optional<Ticket> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    /**
     * Lista as passagens visíveis.
     *
     * @param companyId empresa à qual a listagem é restrita, ou {@code null} para todas
     * @return passagens encontradas
     */
    @Query("""
            select item from Ticket item
            where item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    List<Ticket> findAllVisible(@Param("companyId") UUID companyId);

    /**
     * Verifica se já existe uma passagem cadastrada com o código informado.
     *
     * <p>Considera também as passagens excluídas logicamente.</p>
     *
     * @param code código da passagem
     * @return {@code true} se o código já estiver em uso
     */
    boolean existsByCode(String code);

    /**
     * Passagem que ocupa o assento na viagem, se houver.
     *
     * <p>Passagens canceladas liberam o assento novamente.</p>
     *
     * @param tripId identificador da viagem
     * @param seatId identificador do assento
     * @return passagem que ocupa o assento, se houver
     */
    @Query("""
            select item from Ticket item
            where item.tripId = :tripId and item.seatId = :seatId and item.isDeleted = false
            """)
    Optional<Ticket> findActiveByTripIdAndSeatId(@Param("tripId") UUID tripId, @Param("seatId") UUID seatId);
}
