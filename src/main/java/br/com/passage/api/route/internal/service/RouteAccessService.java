package br.com.passage.api.route.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável por definir o escopo de empresa das consultas de rotas.
 *
 * <ul>
 *     <li>FLEET_MANAGER – acessa apenas as rotas da empresa à qual está vinculado;</li>
 *     <li>demais perfis – acessam as rotas de todas as empresas.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RouteAccessService {

    private final IamApi iamApi;

    /**
     * Recupera a empresa à qual as consultas do usuário autenticado são restritas.
     *
     * @return empresa do gestor de frota, ou {@code null} para os demais perfis
     * @throws AccessDeniedException se o gestor de frota não tiver empresa vinculada
     */
    public UUID visibleCompanyUuid() {
        CurrentUser user = iamApi.currentUser();

        if (user.role() != UserRole.FLEET_MANAGER) {
            return null;
        }

        if (user.companyUuid() == null) {
            throw new AccessDeniedException("Gestor sem empresa vinculada não pode acessar rotas.");
        }

        return user.companyUuid();
    }
}