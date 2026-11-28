package br.com.passage.api.route.internal.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.dto.RouteResponse;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela consulta de rotas.
 *
 * <p>Rotas excluídas não são retornadas.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link RouteAccessService}: o gestor de frota acessa
 * apenas as rotas da própria empresa e os demais perfis acessam as rotas de todas as empresas.</p>
 */
@Service 
@RequiredArgsConstructor 
public class FindRouteService {
    
    private final RouteRepository routeRepository;
    private final RouteAccessService routeAccessService;

    /**
     * Busca uma rota visível ao usuário autenticado.
     *
     * @param id identificador da rota
     * @return rota encontrada
     * @throws ResourceNotFoundException se a rota não existir, estiver excluída ou pertencer a outra empresa
     */
    @Transactional(readOnly = true)
    public RouteResponse execute(UUID id){
        Route route = routeRepository.findVisibleById(id, routeAccessService.visibleCompanyUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada com o identificador: " + id));
        return RouteResponse.from(route);
    }

    /**
     * Lista as rotas visíveis ao usuário autenticado.
     *
     * @return rotas encontradas
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> executeAll(){
        return routeRepository.findAllVisible(routeAccessService.visibleCompanyUuid())
                .stream()
                .map(RouteResponse::from)
                .toList();
    }
}
