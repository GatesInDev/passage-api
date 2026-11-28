package br.com.passage.api.ticket;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * API pública do módulo de passagens.
 *
 * <p>Ponto de acesso para que outros módulos consultem passagens sem
 * depender das classes internas do módulo.</p>
 */
@Component
@RequiredArgsConstructor
public class TicketApi {

    private final TicketRepository ticketRepository;

    /**
     * Recupera uma passagem pelo identificador.
     *
     * <p>A consulta não aplica isolamento por empresa: quem chama é outro
     * módulo do sistema, e não um usuário navegando pela bilheteria.</p>
     *
     * @throws ResourceNotFoundException se a passagem não existir ou estiver excluída
     */
    public TicketInfo findTicket(UUID ticketId) {
        return ticketRepository.findVisibleById(ticketId, null)
                .map(ticket -> new TicketInfo(
                        ticket.getId(),
                        ticket.getCode(),
                        ticket.getPassengerName(),
                        ticket.getPassengerDocument(),
                        ticket.getTripId(),
                        ticket.getSeatId(),
                        ticket.getPrice(),
                        ticket.getCompanyUuid(),
                        ticket.getDepartureDateTime()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passagem não encontrada com o identificador: " + ticketId));
    }
}
