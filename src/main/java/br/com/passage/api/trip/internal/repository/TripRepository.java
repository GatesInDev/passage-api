package br.com.passage.api.trip.internal.repository;

import java.time.Instant;
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

    @Query("""
        select count(t) > 0 from Trip t
        where t.busId = :busId
        and t.isDeleted = false
        and t.status <> br.com.passage.api.trip.enums.TripStatus.CANCELED
        and t.departureTime < :arrivalTime
        and t.arrivalTime > :departureTime
        and (:excludedId is null or t.id <> :excludedId)
        """)
    boolean existsBusConflict(@Param("busId") UUID busId,
                              @Param("departureTime") Instant departureTime,
                              @Param("arrivalTime") Instant arrivalTime,
                              @Param("excludedId") UUID excludedId);

    @Query("""
        select count(t) > 0 from Trip t
        where t.driverId = :driverId
        and t.isDeleted = false
        and t.status <> br.com.passage.api.trip.enums.TripStatus.CANCELED
        and t.departureTime < :arrivalTime
        and t.arrivalTime > :departureTime
        and (:excludedId is null or t.id <> :excludedId)
        """)
    boolean existsDriverConflict(@Param("driverId") UUID driverId,
                                 @Param("departureTime") Instant departureTime,
                                 @Param("arrivalTime") Instant arrivalTime,
                                 @Param("excludedId") UUID excludedId);

    /**
     * Verifica se já existe uma viagem cadastrada com o código informado.
     *
     * @param code código da viagem
     * @return {@code true} se o código já estiver em uso
     */
    boolean existsByCode(String code);
}
