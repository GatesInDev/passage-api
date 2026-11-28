package br.com.passage.api.route.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.dto.CreateRouteRequest;
import br.com.passage.api.route.internal.dto.RouteResponse;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;

/**
 * Serviço responsável pelo cadastro de rotas.
 *
 * <p>O código deve ser único no sistema e a origem, o destino e a abrangência são validados
 * pelo {@link RouteConsistencyValidator}.</p>
 *
 * <h2>Empresa</h2>
 *
 * <p>O gestor de frota cadastra sempre na própria empresa, ignorando o {@code companyUuid}
 * informado. O administrador deve informar a empresa da rota.</p>
 */
@Service
@AllArgsConstructor
public class CreateRouteService {

    private final RouteConsistencyValidator routeConsistencyValidator;
    private final RouteRepository routeRepository;
    private final IamApi iamApi;

    /**
     * Cadastra uma rota.
     *
     * @param request dados da rota
     * @return rota cadastrada
     * @throws BusinessException se o código já estiver em uso, a empresa não for definida ou a
     *                           rota for incoerente
     */
    @Transactional
    public RouteResponse execute(CreateRouteRequest request){

        if(routeRepository.existsByCode(request.code())){
            throw new BusinessException("Já existe uma rota cadastrada com o código " + request.code());
        }

        CurrentUser user = iamApi.currentUser();
        UUID companyUuid;

        if(user.role() == UserRole.ADMIN){
            companyUuid = request.companyUuid();
            if(companyUuid == null){
                throw new BusinessException("Para usuários ADMIN, o UUID da empresa deve ser informado no payload.");
            }
        }else{
            companyUuid = user.companyUuid();
            if(companyUuid == null){
                throw new BusinessException("Usuário sem empresa vinculada não pode cadastrar rotas.");
            }
        }

        routeConsistencyValidator.validate(request.originCity(), request.originState(),
                request.destinationCity(), request.destinationState(), request.serviceScope());

        Route route = new Route(
            request.code(),
            companyUuid,
            request.name(),
            request.originCity(),
            request.originState(), 
            request.destinationCity(),
            request.destinationState(),
            request.distanceKm(),
            request.estimatedDurationMinutes(),
            request.serviceScope(),
            request.regulatoryAgency()
        );

        Route savedRoute = routeRepository.save(route);

        return RouteResponse.from(savedRoute);
    }
}
