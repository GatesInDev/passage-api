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

    @Transactional(readOnly = true)
    public TripResponse execute(UUID id){
        Trip trip = tripRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));
        return TripResponse.from(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> executeAll(){
        return tripRepository.findAll().stream()
                .filter(trip -> !trip.isDeleted())
                .map(TripResponse::from)
                .toList();
    }
}
