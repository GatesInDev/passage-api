package br.com.passage.api.route.internal.service;

import org.springframework.stereotype.Service;

import br.com.passage.api.route.internal.domain.enums.RouteScope;
import br.com.passage.api.shared.domain.exceptions.BusinessException;

/**
 * Validador da coerência entre a origem, o destino e a abrangência de uma rota.
 *
 * <h2>Regras</h2>
 *
 * <ul>
 *     <li>a origem e o destino não podem ser a mesma cidade do mesmo estado;</li>
 *     <li>rotas {@code INTRASTATE} e {@code METROPOLITAN} devem começar e terminar no mesmo estado;</li>
 *     <li>rotas {@code INTERSTATE} devem ligar estados diferentes.</li>
 * </ul>
 *
 * <p>As cidades são comparadas sem diferenciar maiúsculas e minúsculas e desconsiderando os
 * espaços nas pontas. Rotas {@code INTERNATIONAL} não têm a abrangência validada.</p>
 */
@Service
public class RouteConsistencyValidator {

    /**
     * Valida a coerência da rota.
     *
     * @param originCity       cidade de origem
     * @param originState      estado de origem
     * @param destinationCity  cidade de destino
     * @param destinationState estado de destino
     * @param scope            abrangência da rota
     * @throws BusinessException se alguma das regras for violada
     */
    public void validate(String originCity, String originState, String destinationCity, String destinationState, RouteScope scope) {
        boolean sameState = originState.equalsIgnoreCase(destinationState);
        boolean sameCity = originCity.trim().equalsIgnoreCase(destinationCity.trim());

        if (sameCity && sameState) {
            throw new BusinessException("A origem e o destino da rota não podem ser iguais.");
        }

        if ((scope == RouteScope.INTRASTATE || scope == RouteScope.METROPOLITAN) && !sameState) {
            throw new BusinessException("Rotas estaduais e metropolitanas devem começar e terminar no mesmo estado.");
        }

        if (scope == RouteScope.INTERSTATE && sameState) {
            throw new BusinessException("Rotas interestaduais devem ligar estados diferentes.");
        }
    }
}
