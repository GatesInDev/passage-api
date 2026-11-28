package br.com.passage.api.route;

import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * API pública do módulo de rotas.
 *
 * <p>Ponto de acesso para que outros módulos consultem informações das rotas sem depender
 * das classes internas do módulo.</p>
 *
 * <p>As consultas não aplicam o escopo do usuário autenticado: o módulo que chama é
 * responsável por validar o acesso conforme a própria regra.</p>
 */
@Component
@RequiredArgsConstructor
public class RouteApi {

    private final RouteRepository routeRepository;

    /**
     * Recupera a empresa de uma rota disponível para receber viagens.
     *
     * @param routeId identificador da rota
     * @return empresa da rota
     * @throws ResourceNotFoundException se a rota não existir ou estiver excluída
     * @throws BusinessException         se a rota estiver inativa
     */
    public UUID findCompanyUuid(UUID routeId){

        Route route = routeRepository.findVisibleById(routeId, null)
            .orElseThrow(() -> new ResourceNotFoundException("Rota não encontrada com o identificador: " + routeId));

        if (!route.isActive()) {
            throw new BusinessException("A rota informada está inativa.");
        }

        return route.getCompanyUuid();
    }
}
