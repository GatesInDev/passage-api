package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.dto.TripResponse;
import br.com.passage.api.trip.internal.dto.UpdateTripRequest;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UpdateTripService {
    
    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;
    
    @Transactional 
    public TripResponse execute(UUID id, UpdateTripRequest request){
        Trip trip = tripRepository.findVisibleById(id, tripAccessService.visibleCompanyUuid())
            .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));

        if(!request.arrivalTime().isAfter(request.departureTime())){
            throw new BusinessException("O horário de chegada deve ser posterior ao horário de partida.");
        }

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
