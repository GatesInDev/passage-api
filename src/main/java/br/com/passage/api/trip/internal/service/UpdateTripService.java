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
 * Atualiza uma viagem visível ao usuário logado; o código e a rota não mudam.
 *
 * <p>Viagens finalizadas não aceitam alterações. O cancelamento apenas altera a situação
 * e dispensa as demais validações, para que uma viagem possa ser cancelada mesmo com
 * o ônibus ou o motorista desativados.</p>
 */
@Service 
@RequiredArgsConstructor 
public class UpdateTripService {
    
    private final TripFleetValidator tripFleetValidator;
    private final TripScheduleValidator tripScheduleValidator;
    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;
    
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
