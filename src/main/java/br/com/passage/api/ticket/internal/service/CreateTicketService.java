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
 * Serviço responsável pela venda de passagens.
 *
 * <p>O preço, a data de partida e a empresa da passagem são obtidos da viagem no momento da
 * venda. Após a venda, é publicado o evento {@link TicketSold}, que dispara a emissão do
 * documento fiscal.</p>
 *
 * <h2>Validações</h2>
 *
 * <ul>
 *     <li>o código deve ser único no sistema;</li>
 *     <li>a viagem deve estar agendada ou em embarque;</li>
 *     <li>quem não é administrador só vende passagens de viagens da própria empresa;</li>
 *     <li>o assento deve pertencer ao ônibus da viagem e não pode estar vendido.</li>
 * </ul>
 */
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
    private final FleetApi fleetApi;
    private final ApplicationEventPublisher events;

    /**
     * Vende uma passagem.
     *
     * @param request dados da passagem
     * @return passagem vendida
     * @throws ResourceNotFoundException se a viagem não existir ou estiver excluída
     * @throws AccessDeniedException     se a viagem pertencer a outra empresa
     * @throws BusinessException         se alguma das validações falhar
     */
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
