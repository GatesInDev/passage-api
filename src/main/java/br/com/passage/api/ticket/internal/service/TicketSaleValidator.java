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
 * Validador das regras comuns à venda e à alteração de uma passagem.
 *
 * <h2>Regras</h2>
 *
 * <ul>
 *     <li>a viagem deve estar agendada ou em embarque;</li>
 *     <li>quem não é administrador só opera viagens da própria empresa;</li>
 *     <li>o assento deve pertencer ao ônibus da viagem;</li>
 *     <li>o assento não pode estar ocupado por outra passagem na viagem.</li>
 * </ul>
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
     * Valida a venda e retorna a viagem, de onde vêm o preço, a partida e a empresa.
     *
     * @param tripId   identificador da viagem
     * @param seatId   identificador do assento
     * @param ticketId passagem sendo alterada, ou {@code null} em uma venda
     * @return dados da viagem
     * @throws ResourceNotFoundException se a viagem não existir ou estiver excluída
     * @throws AccessDeniedException     se a viagem pertencer a outra empresa
     * @throws BusinessException         se alguma das regras for violada
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

    /**
     * Verifica se o assento está ocupado por outra passagem na viagem.
     *
     * <p>A própria passagem, quando alterada, não conta como ocupante.</p>
     *
     * @param tripId   identificador da viagem
     * @param seatId   identificador do assento
     * @param ticketId passagem desconsiderada, ou {@code null}
     * @return {@code true} se o assento estiver ocupado
     */
    private boolean isSeatTaken(UUID tripId, UUID seatId, UUID ticketId) {
        return ticketRepository.findActiveByTripIdAndSeatId(tripId, seatId)
                .filter(occupant -> !occupant.getId().equals(ticketId))
                .isPresent();
    }
}
