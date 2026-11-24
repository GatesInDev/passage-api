package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    @Query("""
            select distinct item from Driver item
            join fetch item.company company
            where item.id = :id and item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    Optional<Driver> findVisibleById(@Param("id") UUID id, @Param("companyId") UUID companyId);

    @Query("""
            select distinct item from Driver item
            join fetch item.company company
            where item.isDeleted = false and company.isDeleted = false
            and (:companyId is null or company.id = :companyId)
            """)
    List<Driver> findAllVisible(@Param("companyId") UUID companyId);

    boolean existsByLicenseNumber(String value);

    boolean existsByLicenseNumberAndIdNot(String value, UUID id);
}
