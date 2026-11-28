package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.ticket.TicketSold;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.CreateTicketRequest;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import br.com.passage.api.trip.TripInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela venda de passagens.
 *
 * <p>O preço, a data de partida e a empresa da passagem são obtidos da viagem no momento da
 * venda. Após a venda, é publicado o evento {@link TicketSold}, que dispara a emissão do
 * documento fiscal.</p>
 *
 * <p>O código deve ser único no sistema. As demais regras da venda são aplicadas pelo
 * {@link TicketSaleValidator}.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateTicketService {

    private final TicketRepository ticketRepository;
    private final TicketSaleValidator ticketSaleValidator;
    private final ApplicationEventPublisher events;

    /**
     * Vende uma passagem.
     *
     * @param request dados da passagem
     * @return passagem vendida
     * @throws ResourceNotFoundException se a viagem não existir ou estiver excluída
     * @throws AccessDeniedException     se a viagem pertencer a outra empresa
     * @throws BusinessException         se o código já estiver em uso ou alguma regra da venda falhar
     */
    @Transactional
    public TicketResponse execute(CreateTicketRequest request) {

        if (ticketRepository.existsByCode(request.code())) {
            throw new BusinessException("Já existe uma passagem cadastrada com o código " + request.code());
        }

        // Preço, horário e empresa vêm da viagem, nunca do cliente.
        TripInfo trip = ticketSaleValidator.validate(request.tripId(), request.seatId(), null);

        Ticket ticket = Ticket.builder()
                .code(request.code())
                .passengerName(request.passengerName())
                .passengerDocument(request.passengerDocument())
                .seatId(request.seatId())
                .price(trip.price())
                .departureDateTime(trip.departureTime())
                .tripId(trip.id())
                .companyUuid(trip.companyUuid())
                // @SuperBuilder ignora o valor padrão de isActive da EntityBase.
                .isActive(true)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        // O documento fiscal é emitido depois do commit, fora desta transação.
        events.publishEvent(new TicketSold(saved.getId(), saved.getCompanyUuid()));

        return TicketResponse.from(saved);
    }
}
