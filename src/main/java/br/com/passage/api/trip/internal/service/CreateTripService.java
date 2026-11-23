package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.route.RouteApi;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.domain.enums.TripStatus;
import br.com.passage.api.trip.internal.dto.CreateTripRequest;
import br.com.passage.api.trip.internal.dto.TripResponse;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor
public class CreateTripService {
    
    private final TripRepository tripRepository;
    private final IamApi iamApi;
    private final RouteApi routeApi;

    @Transactional 
    public TripResponse execute(CreateTripRequest request){
        
        if(tripRepository.existsByCode(request.code())){
            throw new BusinessException("Já existe uma viagem cadastrada com o código " + request.code());
        }

        CurrentUser user = iamApi.currentUser();
        UUID companyUuid = routeApi.findCompanyUuid(request.routeId());

        if(user.role() != UserRole.ADMIN && !companyUuid.equals(user.companyUuid())){
            throw new AccessDeniedException("Usuário não tem permissão para criar uma viagem para esta rota.");
        }
        
        if(!request.arrivalTime().isAfter(request.departureTime())){
            throw new BusinessException("O horário de chegada deve ser posterior ao horário de partida.");
        }
        
        Trip trip = new Trip(
            request.code(),
            request.routeId(),
            companyUuid,
            request.busId(),
            request.driverId(),
            request.departureTime(),
            request.arrivalTime(),
            TripStatus.SCHEDULED,
            request.price()
        );

        Trip savedTrip = tripRepository.save(trip);
        return TripResponse.from(savedTrip);
    }
}
