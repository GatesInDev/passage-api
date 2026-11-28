package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.dto.UpdateTicketRequest;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import br.com.passage.api.trip.TripInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela atualização de passagens.
 *
 * <p>Permite alterar os dados do passageiro, o assento, o preço, a partida, a viagem e a
 * situação da passagem. O código e a empresa não são alterados.</p>
 */
@Service
@RequiredArgsConstructor
public class UpdateTicketService {

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;
    private final TicketSaleValidator ticketSaleValidator;

    /**
     * Atualiza uma passagem visível ao usuário autenticado.
     *
     * @param id      identificador da passagem
     * @param request novos dados da passagem
     * @return passagem atualizada
     * @throws ResourceNotFoundException se a passagem não existir, estiver excluída ou pertencer a outra empresa
     */
    @Transactional
    public TicketResponse execute(UUID id, UpdateTicketRequest request) {

        Ticket ticket = ticketRepository.findVisibleById(id, ticketAccessService.currentCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Passagem não encontrada com o identificador: " + id));

        // A própria passagem é ignorada na checagem de assento ocupado.
        TripInfo trip = ticketSaleValidator.validate(request.tripId(), request.seatId(), ticket.getId());

        ticket.setPassengerName(request.passengerName());
        ticket.setPassengerDocument(request.passengerDocument());
        ticket.setSeatId(request.seatId());
        ticket.setTripId(trip.id());
        ticket.setPrice(trip.price());
        ticket.setDepartureDateTime(trip.departureTime());
        ticket.setCompanyUuid(trip.companyUuid());
        ticket.setActive(request.isActive());

        Ticket updated = ticketRepository.save(ticket);

        return TicketResponse.from(updated);
    }
}
