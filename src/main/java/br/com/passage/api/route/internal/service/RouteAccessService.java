package br.com.passage.api.route.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import lombok.RequiredArgsConstructor;

/**
 * Define o escopo de empresa das consultas de rotas para o usuário logado.
 */
@Service
@RequiredArgsConstructor
public class RouteAccessService {

    private final IamApi iamApi;

    /**
     * @return a empresa do gestor, ou {@code null} (sem restrição) para os demais perfis,
     *         que consultam as rotas de todas as empresas
     * @throws AccessDeniedException se o gestor não tiver empresa vinculada
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