package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.CreateTicketRequest;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CreateTicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public TicketResponse execute(CreateTicketRequest request) {

        if (ticketRepository.existsByCode(request.code())) {
            throw new BusinessException("Já existe uma passagem cadastrada com o código " + request.code());
        }

        Ticket ticket = Ticket.builder()
                .code(request.code())
                .passengerName(request.passengerName())
                .passengerDocument(request.passengerDocument())
                .seatId(request.seatId())
                .price(request.price())
                .departureDateTime(request.departureDateTime())
                .routeId(request.routeId())
                // @SuperBuilder ignora o valor inicial de isActive em EntityBase,
                // por isso a passagem precisa nascer ativa explicitamente.
                .isActive(true)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        return TicketResponse.from(saved);
    }
}