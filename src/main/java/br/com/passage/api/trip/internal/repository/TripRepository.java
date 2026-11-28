package br.com.passage.api.trip.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.passage.api.trip.internal.domain.entities.Trip;

/**
 * Repositório de acesso às viagens.
 */
public interface TripRepository extends JpaRepository<Trip, UUID> {

    @Query("""
        select t from Trip t
        where t.id = :id and t.isDeleted = false
        and (:companyUuid is null or t.companyUuid = :companyUuid)
        """)
    Optional<Trip> findVisibleById(@Param("id") UUID id, @Param("companyUuid") UUID companyUuid);

    @Query("""
        select t from Trip t
        where t.isDeleted = false
        and (:companyUuid is null or t.companyUuid = :companyUuid)
        """)
    List<Trip> findAllVisible(@Param("companyUuid") UUID companyUuid);

    /**
     * Verifica se já existe uma viagem cadastrada com o código informado.
     *
     * @param code código da viagem
     * @return {@code true} se o código já estiver em uso
     */
    boolean existsByCode(String code);
}
