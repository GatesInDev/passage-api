package br.com.passage.api.ticket;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Dados de uma passagem expostos a outros módulos.
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
