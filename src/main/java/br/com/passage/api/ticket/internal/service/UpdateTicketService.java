package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.dto.UpdateTicketRequest;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
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

        ticket.setPassengerName(request.passengerName());
        ticket.setPassengerDocument(request.passengerDocument());
        ticket.setSeatId(request.seatId());
        ticket.setPrice(request.price());
        ticket.setDepartureDateTime(request.departureDateTime());
        ticket.setTripId(request.tripId());
        ticket.setActive(request.isActive());

        Ticket updated = ticketRepository.save(ticket);

        return TicketResponse.from(updated);
    }
}
