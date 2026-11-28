package br.com.passage.api.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.trip.enums.TripStatus;

/**
 * Dados de uma viagem expostos aos demais módulos, usados principalmente na venda de passagens.
 *
 * @param id            identificador da viagem
 * @param companyUuid   empresa operadora, herdada da rota
 * @param busId         ônibus alocado
 * @param departureTime partida prevista (UTC)
 * @param price         preço base da passagem
 * @param status        situação atual
 */
public record TripInfo(
        UUID id,
        UUID companyUuid,
        UUID busId,
        Instant departureTime,
        BigDecimal price,
        TripStatus status
) {}