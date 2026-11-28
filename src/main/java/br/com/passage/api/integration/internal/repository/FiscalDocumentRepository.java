package br.com.passage.api.integration.internal.repository;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FiscalDocumentRepository extends JpaRepository<FiscalDocument, UUID> {

    /**
     * Documento visível ao usuário atual.
     *
     * <p>{@code companyId} nulo representa um administrador, que enxerga
     * os documentos de todas as empresas.</p>
     */
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

    @Query("select coalesce(max(item.number), 0) from FiscalDocument item where item.series = :series")
    long lastNumberOfSeries(@Param("series") Integer series);
}
