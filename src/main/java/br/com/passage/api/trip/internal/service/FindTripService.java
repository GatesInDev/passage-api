package br.com.passage.api.trip.internal.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.dto.TripResponse;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class FindTripService {
    
    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;

    @Transactional(readOnly = true)
    public TripResponse execute(UUID id){
        Trip trip = tripRepository.findVisibleById(id, tripAccessService.visibleCompanyUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));
        return TripResponse.from(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> executeAll(){
        return tripRepository.findAllVisible(tripAccessService.visibleCompanyUuid()).stream()
                .map(TripResponse::from)
                .toList();
    }
}
