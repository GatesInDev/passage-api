package br.com.passage.api.ticket;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Dados de uma passagem expostos aos demais módulos.
 *
 * @param price             preço pago, copiado da viagem na venda
 * @param companyUuid       empresa dona da passagem, a mesma da viagem
 * @param departureDateTime partida da viagem registrada na venda
 */
public record TicketInfo(
        UUID id,
        String code,
        String passengerName,
        String passengerDocument,
        UUID tripId,
        UUID seatId,
        BigDecimal price,
        UUID companyUuid,
        Instant departureDateTime
) {}
