package br.com.passage.api.trip;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.passage.api.trip.enums.TripStatus;

/**
 * Representa os dados de uma viagem expostos aos demais módulos.
 *
 * <p>Utilizado principalmente na venda de passagens, que obtém da viagem o preço, a partida
 * e a empresa.</p>
 *
 * @param id            identificador da viagem
 * @param companyUuid   empresa responsável pela viagem, a mesma da rota
 * @param busId         ônibus alocado para a viagem
 * @param departureTime data e hora de partida prevista, em UTC
 * @param price         preço base da passagem
 * @param status        situação atual da viagem
 */
public record TripInfo(
        UUID id,
        UUID companyUuid,
        UUID busId,
        Instant departureTime,
        BigDecimal price,
        TripStatus status
) {}