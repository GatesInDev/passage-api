package br.com.passage.api.route;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class RouteApi {
    
    private final RouteRepository routeRepository;

    public UUID findCompanyUuid(UUID routeId){

        Route route = routeRepository.findVisibleById(routeId, null)
            .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada com o identificador: " + routeId));
        
        if (!route.isActive()) {                                                
            throw new BusinessException("A rota informada está inativa.");
        }

        return route.getCompanyUuid();
    }
}
