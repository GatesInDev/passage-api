package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de acesso aos ônibus.
 *
 * <p>As consultas {@code Visible} desconsideram ônibus excluídos e ônibus de empresas
 * excluídas. Quando {@code companyId} é informado, o resultado é restrito a essa empresa;
 * quando é {@code null}, todas as empresas são consideradas.</p>
 */
public interface BusRepository extends JpaRepository<Bus, UUID> {

    /**
     * Busca um ônibus visível, carregando a empresa e os assentos.
     *
     * @param id        identificador do ônibus
     * @param companyId empresa à qual a busca é restrita, ou {@code null} para todas
     * @return ônibus encontrado, se existir
     */
    @Query("""
            select distinct item from Bus item
            join fetch item.company company
            left join fetch item.seats
            where item.id = :id and item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    Optional<Bus> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    /**
     * Lista os ônibus visíveis, carregando a empresa e os assentos.
     *
     * @param companyId empresa à qual a listagem é restrita, ou {@code null} para todas
     * @return ônibus encontrados
     */
    @Query("""
            select distinct item from Bus item
            join fetch item.company company
            left join fetch item.seats
            where item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    List<Bus> findAllVisible(@Param("companyId") UUID companyId);

    /**
     * Verifica se já existe um ônibus cadastrado com a placa informada.
     *
     * <p>Considera também os ônibus excluídos logicamente.</p>
     *
     * @param value placa do ônibus
     * @return {@code true} se a placa já estiver em uso
     */
    boolean existsByLicensePlate(String value);

    /**
     * Verifica se a placa informada já está em uso por outro ônibus.
     *
     * @param value placa do ônibus
     * @param id    ônibus desconsiderado na verificação
     * @return {@code true} se a placa já estiver em uso por outro ônibus
     */
    boolean existsByLicensePlateAndIdNot(String value, UUID id);
}
