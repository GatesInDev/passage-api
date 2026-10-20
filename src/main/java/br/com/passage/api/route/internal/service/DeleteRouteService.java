package br.com.passage.api.route.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.route.internal.domain.entities.Route;
import br.com.passage.api.route.internal.repository.RouteRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteRouteService {
    
    private final RouteRepository routeRepository;
    private final IamApi iamApi;

    @Transactional 
    public void execute(UUID id){
        Route route = routeRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new BusinessException("Rota não encontrada com o identificador: " + id));

        CurrentUser user = iamApi.currentUser();

        if(user.role() != UserRole.ADMIN && !route.belongsTo(user.companyUuid())){
            throw new AccessDeniedException("Usuário não tem permissão para deletar esta rota.");
        }

        route.setDeleted(true);
        route.setActive(false);
        routeRepository.save(route);

    }
}
