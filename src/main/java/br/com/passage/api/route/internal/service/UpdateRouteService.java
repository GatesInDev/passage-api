package br.com.passage.api.route.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.dto.RouteResponse;
import br.com.passage.api.route.internal.dto.UpdateRouteRequest;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela atualização de rotas.
 *
 * <p>Permite alterar os dados do trajeto e a situação da rota. O código e a empresa não são
 * alterados, e a coerência da rota é validada novamente pelo {@link RouteConsistencyValidator}.</p>
 */
@Service
@RequiredArgsConstructor 
public class UpdateRouteService {
    
    private final RouteConsistencyValidator routeConsistencyValidator;
    private final RouteRepository routeRepository;
    private final RouteAccessService routeAccessService;

    /**
     * Atualiza uma rota visível ao usuário autenticado.
     *
     * @param id      identificador da rota
     * @param request novos dados da rota
     * @return rota atualizada
     * @throws ResourceNotFoundException se a rota não existir, estiver excluída ou pertencer a outra empresa
     * @throws BusinessException         se a rota for incoerente
     */
    @Transactional 
    public RouteResponse execute(UUID id, UpdateRouteRequest request){
        Route route = routeRepository.findVisibleById(id, routeAccessService.visibleCompanyUuid())
             .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada com o identificador: " + id));

        routeConsistencyValidator.validate(request.originCity(), request.originState(),
                request.destinationCity(), request.destinationState(), request.serviceScope());
        
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
