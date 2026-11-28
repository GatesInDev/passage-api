package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.fleet.FleetApi;
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

/**
 * Vende uma passagem.
 *
 * <p>Preço, partida e empresa vêm da viagem, nunca do cliente. Depois de salvar, publica
 * {@link TicketSold} para a emissão do documento fiscal.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateTicketService {

    /** Situações em que a viagem aceita venda. */
    private static final Set<TripStatus> OPEN_FOR_SALE =
            Set.of(TripStatus.SCHEDULED, TripStatus.BOARDING);

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;
    private final TripApi tripApi;
    private final FleetApi fleetApi;
    private final ApplicationEventPublisher events;

    @Transactional
    public TicketResponse execute(CreateTicketRequest request) {

        if (ticketRepository.existsByCode(request.code())) {
            throw new BusinessException("Já existe uma passagem cadastrada com o código " + request.code());
        }

        TripInfo trip = tripApi.findTrip(request.tripId());

        if (!OPEN_FOR_SALE.contains(trip.status())) {
            throw new BusinessException(
                    "A viagem não está disponível para venda. Situação atual: " + trip.status() + ".");
        }

        UUID operatorCompany = ticketAccessService.currentCompanyId();
        if (operatorCompany != null && !operatorCompany.equals(trip.companyUuid())) {
            throw new AccessDeniedException("Não é possível vender passagens de viagens de outra empresa.");
        }

        if (!fleetApi.seatBelongsToBus(request.seatId(), trip.busId())) {
            throw new BusinessException("O assento informado não pertence ao ônibus da viagem.");
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
                // @SuperBuilder ignora o valor padrão de isActive da EntityBase.
                .isActive(true)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        // O documento fiscal é emitido depois do commit: a venda não espera nem falha se a SEFAZ estiver fora.
        events.publishEvent(new TicketSold(saved.getId(), saved.getCompanyUuid()));

        return TicketResponse.from(saved);
    }
}
