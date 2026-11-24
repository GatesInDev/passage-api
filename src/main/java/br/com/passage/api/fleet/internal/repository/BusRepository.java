package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusRepository extends JpaRepository<Bus, UUID> {

    @Query("""
            select distinct item from Bus item
            join fetch item.company company
            left join fetch item.seats
            where item.id = :id and item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    Optional<Bus> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("""
            select distinct item from Bus item
            join fetch item.company company
            left join fetch item.seats
            where item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    List<Bus> findAllVisible(@Param("companyId") UUID companyId);

    boolean existsByLicensePlate(String value);

    boolean existsByLicensePlateAndIdNot(String value, UUID id);
}
