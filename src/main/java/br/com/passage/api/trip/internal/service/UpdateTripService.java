package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.enums.TripStatus;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.dto.TripResponse;
import br.com.passage.api.trip.internal.dto.UpdateTripRequest;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela atualização de viagens.
 *
 * <p>Permite alterar o ônibus, o motorista, os horários, o preço e a situação da viagem. O código
 * e a rota não são alterados.</p>
 *
 * <h2>Situação</h2>
 *
 * <ul>
 *     <li>viagens concluídas ou canceladas não aceitam alterações;</li>
 *     <li>a mudança de situação segue as transições de {@link TripStatus#canTransitionTo};</li>
 *     <li>no cancelamento, apenas a situação é alterada e os demais campos são ignorados.</li>
 * </ul>
 *
 * <p>Nas demais alterações, a viagem passa pelas mesmas validações do agendamento.</p>
 */
@Service 
@RequiredArgsConstructor 
public class UpdateTripService {
    
    private final TripFleetValidator tripFleetValidator;
    private final TripScheduleValidator tripScheduleValidator;
    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;
    
    /**
     * Atualiza uma viagem visível ao usuário autenticado.
     *
     * @param id      identificador da viagem
     * @param request novos dados da viagem
     * @return viagem atualizada
     * @throws ResourceNotFoundException se a viagem, o ônibus ou o motorista não forem encontrados
     * @throws BusinessException         se a viagem estiver finalizada, a transição de situação for
     *                                   inválida ou alguma das validações falhar
     */
    @Transactional 
    public TripResponse execute(UUID id, UpdateTripRequest request){
        Trip trip = tripRepository.findVisibleById(id, tripAccessService.visibleCompanyUuid())
            .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));

        trip.ensureEditable();

        if (request.status() == TripStatus.CANCELED) {
            trip.changeStatus(TripStatus.CANCELED);
            return TripResponse.from(tripRepository.save(trip));
        }
        
        tripFleetValidator.validate(trip.getCompanyUuid(), request.busId(), request.driverId());

        if(!request.arrivalTime().isAfter(request.departureTime())){
            throw new BusinessException("O horário de chegada deve ser posterior ao horário de partida.");
        }

        tripScheduleValidator.validate(request.busId(), request.driverId(),
                request.departureTime(), request.arrivalTime(), trip.getId());

        trip.changeStatus(request.status());
        trip.setBusId(request.busId());
        trip.setDriverId(request.driverId());
        trip.setDepartureTime(request.departureTime());
        trip.setArrivalTime(request.arrivalTime());
        trip.setPrice(request.price());

        Trip updatedTrip = tripRepository.save(trip);

        return TripResponse.from(updatedTrip);
    }
}
