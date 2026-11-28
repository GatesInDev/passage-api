package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.fleet.FleetApi;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import br.com.passage.api.trip.TripApi;
import br.com.passage.api.trip.TripInfo;
import br.com.passage.api.trip.enums.TripStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Regras comuns à emissão e à alteração de uma passagem.
 *
 * <p>Alterar uma passagem é vendê-la de novo: a viagem precisa continuar
 * aberta, o assento precisa existir no ônibus dela e não pode estar com
 * outro passageiro.</p>
 */
@Service
@RequiredArgsConstructor
public class TicketSaleValidator {

    /**
     * Situações em que a viagem ainda admite venda.
     *
     * <p>Uma viagem em andamento, concluída ou cancelada não recebe
     * passageiros novos.</p>
     */
    private static final Set<TripStatus> OPEN_FOR_SALE =
            Set.of(TripStatus.SCHEDULED, TripStatus.BOARDING);

    private final TripApi tripApi;
    private final FleetApi fleetApi;
    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    /**
     * Valida a venda e devolve a viagem, que é a fonte do preço e do horário.
     *
     * @param tripId   viagem escolhida
     * @param seatId   assento escolhido
     * @param ticketId passagem sendo alterada, ou {@code null} em uma emissão
     * @return dados da viagem
     */
    public TripInfo validate(UUID tripId, UUID seatId, UUID ticketId) {
        TripInfo trip = tripApi.findTrip(tripId);

        if (!OPEN_FOR_SALE.contains(trip.status())) {
            throw new BusinessException(
                    "A viagem não está disponível para venda. Situação atual: " + trip.status() + ".");
        }

        UUID operatorCompany = ticketAccessService.currentCompanyId();
        if (operatorCompany != null && !operatorCompany.equals(trip.companyUuid())) {
            throw new AccessDeniedException("Não é possível vender passagens de viagens de outra empresa.");
        }

        if (!fleetApi.seatBelongsToBus(seatId, trip.busId())) {
            throw new BusinessException("O assento informado não pertence ao ônibus da viagem.");
        }

        if (isSeatTaken(tripId, seatId, ticketId)) {
            throw new BusinessException("O assento informado já está vendido para esta viagem.");
        }

        return trip;
    }

    /** A própria passagem não conta como ocupante do assento ao ser alterada. */
    private boolean isSeatTaken(UUID tripId, UUID seatId, UUID ticketId) {
        return ticketRepository.findActiveByTripIdAndSeatId(tripId, seatId)
                .filter(occupant -> !occupant.getId().equals(ticketId))
                .isPresent();
    }
}
