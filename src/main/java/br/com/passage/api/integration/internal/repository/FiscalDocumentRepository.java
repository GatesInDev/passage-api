package br.com.passage.api.integration.internal.repository;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso aos documentos fiscais.
 *
 * <p>As consultas {@code Visible} desconsideram registros excluídos e, quando
 * {@code companyId} é informado, restringem o resultado a essa empresa.</p>
 */
public interface FiscalDocumentRepository extends JpaRepository<FiscalDocument, UUID> {

    @Query("""
            select item from FiscalDocument item
            where item.id = :id and item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    Optional<FiscalDocument> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("""
            select item from FiscalDocument item
            where item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    List<FiscalDocument> findAllVisible(@Param("companyId") UUID companyId);

    boolean existsByTicketId(UUID ticketId);

    Optional<FiscalDocument> findByTicketId(UUID ticketId);

    /** Último número usado na série, ou 0 se não houver documento. */
    @Query("select coalesce(max(item.number), 0) from FiscalDocument item where item.series = :series")
    long lastNumberOfSeries(@Param("series") Integer series);
}
