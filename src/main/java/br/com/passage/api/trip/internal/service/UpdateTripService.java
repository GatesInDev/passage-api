package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
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
    private final IamApi iamApi;
    
    @Transactional 
    public TripResponse execute(UUID id, UpdateTripRequest request){
        Trip trip = tripRepository.findById(id)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));
        
        CurrentUser user = iamApi.currentUser();

        if(user.role() != UserRole.ADMIN && !trip.belongsTo(user.companyUuid())){
            throw new AccessDeniedException("Usuário não tem permissão para atualizar esta viagem.");
        }

        if(!request.arrivalTime().isAfter(request.departureTime())){
            throw new BusinessException("O horário de chegada deve ser posterior ao horário de partida.");
        }

        trip.setBusId(request.busId());
        trip.setDriverId(request.driverId());
        trip.setDepartureTime(request.departureTime());
        trip.setArrivalTime(request.arrivalTime());
        trip.setPrice(request.price());
        trip.setStatus(request.status());

        Trip updatedTrip = tripRepository.save(trip);

        return TripResponse.from(updatedTrip);
    }
}
