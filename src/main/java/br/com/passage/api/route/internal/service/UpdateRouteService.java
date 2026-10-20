package br.com.passage.api.route.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.dto.RouteResponse;
import br.com.passage.api.route.internal.dto.UpdateRouteRequest;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class UpdateRouteService {
    
    private final RouteRepository routeRepository;
    private final IamApi iamApi;

    @Transactional 
    public RouteResponse execute(UUID id, UpdateRouteRequest request){
        Route route = routeRepository.findById(id)
             .filter(found -> !found.isDeleted())
             .orElseThrow(() -> new BusinessException("Rota não encontrada com o identificador: " + id));
        
        CurrentUser user = iamApi.currentUser();

        if(user.role() != UserRole.ADMIN && !route.belongsTo(user.companyUuid())){
            throw new AccessDeniedException("Usuário não tem permissão para atualizar esta rota.");
        }

        route.setName(request.name());
        route.setOriginCity(request.originCity());
        route.setOriginState(request.originState());
        route.setDestinationCity(request.destinationCity());
        route.setDestinationState(request.destinationState());
        route.setDistanceKm(request.distanceKm());
        route.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        route.setServiceScope(request.serviceScope());
        route.setRegulatoryAgency(request.regulatoryAgency());
        route.setActive(request.isActive());

        Route updateRoute = routeRepository.save(route);

        return RouteResponse.from(updateRoute);
    }
}
