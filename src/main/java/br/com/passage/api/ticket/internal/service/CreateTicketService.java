package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.ticket.TicketSold;
import br.com.passage.api.ticket.internal.domain.entities.Ticket;
import br.com.passage.api.ticket.internal.dto.CreateTicketRequest;
import br.com.passage.api.ticket.internal.dto.TicketResponse;
import br.com.passage.api.ticket.internal.repository.TicketRepository;
import br.com.passage.api.trip.TripApi;
import br.com.passage.api.trip.TripInfo;
import br.com.passage.api.trip.enums.TripStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTicketService {

    /**
     * Situações em que a viagem ainda admite venda.
     *
     * <p>Uma viagem em andamento, concluída ou cancelada não recebe
     * passageiros novos.</p>
     */
    private static final Set<TripStatus> OPEN_FOR_SALE =
            Set.of(TripStatus.SCHEDULED, TripStatus.BOARDING);

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;
    private final TripApi tripApi;
    private final ApplicationEventPublisher events;

    @Transactional
    public TicketResponse execute(CreateTicketRequest request) {

        if (ticketRepository.existsByCode(request.code())) {
            throw new BusinessException("Já existe uma passagem cadastrada com o código " + request.code());
        }

        // A viagem é a fonte do preço, do horário e da empresa: nenhum
        // desses dados é aceito do cliente.
        TripInfo trip = tripApi.findTrip(request.tripId());

        if (!OPEN_FOR_SALE.contains(trip.status())) {
            throw new BusinessException(
                    "A viagem não está disponível para venda. Situação atual: " + trip.status() + ".");
        }

        UUID operatorCompany = ticketAccessService.currentCompanyId();
        if (operatorCompany != null && !operatorCompany.equals(trip.companyUuid())) {
            throw new AccessDeniedException("Não é possível vender passagens de viagens de outra empresa.");
        }

        if (ticketRepository.existsByTripIdAndSeatIdAndIsDeletedFalse(request.tripId(), request.seatId())) {
            throw new BusinessException("O assento informado já está vendido para esta viagem.");
        }

        Ticket ticket = Ticket.builder()
                .code(request.code())
                .passengerName(request.passengerName())
                .passengerDocument(request.passengerDocument())
                .seatId(request.seatId())
                .price(trip.price())
                .departureDateTime(trip.departureTime())
                .tripId(trip.id())
                .companyUuid(trip.companyUuid())
                // @SuperBuilder ignora o valor inicial de isActive em EntityBase,
                // por isso a passagem precisa nascer ativa explicitamente.
                .isActive(true)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        // A emissão do documento fiscal corre fora desta transação: a venda
        // não deve falhar nem esperar caso a SEFAZ esteja indisponível.
        events.publishEvent(new TicketSold(saved.getId(), saved.getCompanyUuid()));

        return TicketResponse.from(saved);
    }
}
