package br.com.passage.api.route.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.passage.api.route.internal.domain.entities.Route;

/**
 * Repositório de acesso às rotas.
 *
 * <p>As consultas {@code Visible} desconsideram rotas excluídas. Quando {@code companyUuid} é
 * informado, o resultado é restrito a essa empresa; quando é {@code null}, todas as empresas
 * são consideradas.</p>
 */
public interface RouteRepository extends JpaRepository<Route, UUID>{

    /**
     * Busca uma rota visível.
     *
     * @param id          identificador da rota
     * @param companyUuid empresa à qual a busca é restrita, ou {@code null} para todas
     * @return rota encontrada, se existir
     */
    @Query("""
        select r from Route r
        where r.id = :id and r.isDeleted = false
        and (:companyUuid is null or r.companyUuid = :companyUuid)
        """)
    Optional<Route> findVisibleById(@Param("id") UUID id, @Param("companyUuid") UUID companyUuid);

    /**
     * Lista as rotas visíveis.
     *
     * @param companyUuid empresa à qual a listagem é restrita, ou {@code null} para todas
     * @return rotas encontradas
     */
    @Query("""
        select r from Route r
        where r.isDeleted = false
        and (:companyUuid is null or r.companyUuid = :companyUuid)
        """)
    List<Route> findAllVisible(@Param("companyUuid") UUID companyUuid);

    /**
     * Verifica se já existe uma rota cadastrada com o código informado.
     *
     * <p>Considera também as rotas excluídas logicamente.</p>
     *
     * @param code código da rota
     * @return {@code true} se o código já estiver em uso
     */
    boolean existsByCode(String code);
}
