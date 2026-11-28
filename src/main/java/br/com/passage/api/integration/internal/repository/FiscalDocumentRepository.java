package br.com.passage.api.integration.internal.repository;

import br.com.passage.api.integration.internal.domain.entities.FiscalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de acesso aos documentos fiscais.
 *
 * <p>As consultas {@code Visible} desconsideram documentos excluídos. Quando {@code companyId} é
 * informado, o resultado é restrito a essa empresa; quando é {@code null}, todas as empresas
 * são consideradas.</p>
 */
public interface FiscalDocumentRepository extends JpaRepository<FiscalDocument, UUID> {

    /**
     * Busca um documento fiscal visível.
     *
     * @param id        identificador do documento
     * @param companyId empresa à qual a busca é restrita, ou {@code null} para todas
     * @return documento encontrado, se existir
     */
    @Query("""
            select item from FiscalDocument item
            where item.id = :id and item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    Optional<FiscalDocument> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    /**
     * Lista os documentos fiscais visíveis.
     *
     * @param companyId empresa à qual a listagem é restrita, ou {@code null} para todas
     * @return documentos encontrados
     */
    @Query("""
            select item from FiscalDocument item
            where item.isDeleted = false
            and (:companyId is null or item.companyUuid = :companyId)
            """)
    List<FiscalDocument> findAllVisible(@Param("companyId") UUID companyId);

    /**
     * Verifica se a passagem já possui documento fiscal.
     *
     * @param ticketId identificador da passagem
     * @return {@code true} se já houver documento
     */
    boolean existsByTicketId(UUID ticketId);

    /**
     * Busca o documento fiscal de uma passagem.
     *
     * @param ticketId identificador da passagem
     * @return documento encontrado, se existir
     */
    Optional<FiscalDocument> findByTicketId(UUID ticketId);

    /**
     * Recupera o último número utilizado na série.
     *
     * @param series série do documento
     * @return último número da série, ou {@code 0} se não houver documento
     */
    @Query("select coalesce(max(item.number), 0) from FiscalDocument item where item.series = :series")
    long lastNumberOfSeries(@Param("series") Integer series);
}
