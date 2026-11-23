package br.com.passage.api.trip.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.passage.api.trip.internal.domain.entities.Trip;

/**
 * Repositório de acesso às viagens.
 */
public interface TripRepository extends JpaRepository<Trip, UUID> {

    /**
     * Verifica se já existe uma viagem cadastrada com o código informado.
     *
     * @param code código da viagem
     * @return {@code true} se o código já estiver em uso
     */
    boolean existsByCode(String code);
}
