package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de acesso aos motoristas.
 *
 * <p>As consultas {@code Visible} desconsideram motoristas excluídos e motoristas de empresas
 * excluídas. Quando {@code companyId} é informado, o resultado é restrito a essa empresa;
 * quando é {@code null}, todas as empresas são consideradas.</p>
 */
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    /**
     * Busca um motorista visível, carregando a empresa.
     *
     * @param id        identificador do motorista
     * @param companyId empresa à qual a busca é restrita, ou {@code null} para todas
     * @return motorista encontrado, se existir
     */
    @Query("""
            select distinct item from Driver item
            join fetch item.company company
            where item.id = :id and item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    Optional<Driver> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    /**
     * Lista os motoristas visíveis, carregando a empresa.
     *
     * @param companyId empresa à qual a listagem é restrita, ou {@code null} para todas
     * @return motoristas encontrados
     */
    @Query("""
            select distinct item from Driver item
            join fetch item.company company
            where item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    List<Driver> findAllVisible(@Param("companyId") UUID companyId);

    /**
     * Verifica se já existe um motorista cadastrado com a CNH informada.
     *
     * <p>Considera também os motoristas excluídos logicamente.</p>
     *
     * @param value número da CNH
     * @return {@code true} se a CNH já estiver em uso
     */
    boolean existsByLicenseNumber(String value);

    /**
     * Verifica se a CNH informada já está em uso por outro motorista.
     *
     * @param value número da CNH
     * @param id    motorista desconsiderado na verificação
     * @return {@code true} se a CNH já estiver em uso por outro motorista
     */
    boolean existsByLicenseNumberAndIdNot(String value, UUID id);
}
