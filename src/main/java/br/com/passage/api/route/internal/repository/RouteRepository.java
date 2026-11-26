package br.com.passage.api.route.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.passage.api.route.internal.domain.entities.Route;

public interface RouteRepository extends JpaRepository<Route, UUID>{

    @Query("""
        select r from Route r
        where r.id = :id and r.isDeleted = false
        and (:companyUuid is null or r.companyUuid = :companyUuid)
        """)
    Optional<Route> findVisibleById(@Param("id") UUID id, @Param("companyUuid") UUID companyUuid);

    @Query("""
        select r from Route r
        where r.isDeleted = false
        and (:companyUuid is null or r.companyUuid = :companyUuid)
        """)
    List<Route> findAllVisible(@Param("companyUuid") UUID companyUuid);
    
    boolean existsByCode(String code);
} 

