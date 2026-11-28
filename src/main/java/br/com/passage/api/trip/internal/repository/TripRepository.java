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

    /**
     * Busca uma viagem visível.
     *
     * <p>Viagens excluídas são desconsideradas.</p>
     *
     * @param id          identificador da viagem
     * @param companyUuid empresa à qual a busca é restrita, ou {@code null} para todas
     * @return viagem encontrada, se existir
     */
    @Query("""
        select t from Trip t
        where t.id = :id and t.isDeleted = false
        and (:companyUuid is null or t.companyUuid = :companyUuid)
        """)
    Optional<Trip> findVisibleById(@Param("id") UUID id, @Param("companyUuid") UUID companyUuid);

    /**
     * Lista as viagens visíveis.
     *
     * <p>Viagens excluídas são desconsideradas.</p>
     *
     * @param companyUuid empresa à qual a listagem é restrita, ou {@code null} para todas
     * @return viagens encontradas
     */
    @Query("""
        select t from Trip t
        where t.isDeleted = false
        and (:companyUuid is null or t.companyUuid = :companyUuid)
        """)
    List<Trip> findAllVisible(@Param("companyUuid") UUID companyUuid);

    /**
     * Verifica se o ônibus possui outra viagem que se sobrepõe ao período informado.
     *
     * <p>Viagens canceladas ou excluídas não são consideradas. Viagens em sequência, em que uma
     * começa quando a outra termina, não configuram conflito.</p>
     *
     * @param busId         identificador do ônibus
     * @param departureTime início do período
     * @param arrivalTime   fim do período
     * @param excludedId    viagem desconsiderada na verificação, ou {@code null}
     * @return {@code true} se houver conflito de agenda
     */
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

    /**
     * Verifica se o motorista possui outra viagem que se sobrepõe ao período informado.
     *
     * <p>Segue as mesmas regras de {@link #existsBusConflict}.</p>
     *
     * @param driverId      identificador do motorista
     * @param departureTime início do período
     * @param arrivalTime   fim do período
     * @param excludedId    viagem desconsiderada na verificação, ou {@code null}
     * @return {@code true} se houver conflito de agenda
     */
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
