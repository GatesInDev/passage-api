package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.CreateTicketRequest;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTicketService {

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    @Transactional
    public TicketResponse execute(CreateTicketRequest request) {

        if (ticketRepository.existsByCode(request.code())) {
            throw new BusinessException("Já existe uma passagem cadastrada com o código " + request.code());
        }

        if (ticketRepository.existsByTripIdAndSeatIdAndIsDeletedFalse(request.tripId(), request.seatId())) {
            throw new BusinessException("O assento informado já está vendido para esta viagem.");
        }

        UUID companyUuid = ticketAccessService.resolveCompanyForCreation(request.companyUuid());

        Ticket ticket = Ticket.builder()
                .code(request.code())
                .passengerName(request.passengerName())
                .passengerDocument(request.passengerDocument())
                .seatId(request.seatId())
                .price(request.price())
                .departureDateTime(request.departureDateTime())
                .tripId(request.tripId())
                .companyUuid(companyUuid)
                // @SuperBuilder ignora o valor inicial de isActive em EntityBase,
                // por isso a passagem precisa nascer ativa explicitamente.
                .isActive(true)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        return TicketResponse.from(saved);
    }
}