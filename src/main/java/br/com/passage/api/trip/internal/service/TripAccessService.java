package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import lombok.RequiredArgsConstructor;

/** Define o escopo de empresa das consultas de viagens para o usuário logado. */
@Service
@RequiredArgsConstructor
public class TripAccessService {

    private final IamApi iamApi;

    /**
     * @return a empresa do gestor, ou {@code null} (sem restrição) para os demais perfis,
     *         que consultam as viagens de todas as empresas
     * @throws AccessDeniedException se o gestor não tiver empresa vinculada
     */
    public UUID visibleCompanyUuid() {
        CurrentUser user = iamApi.currentUser();

        if (user.role() != UserRole.FLEET_MANAGER) {
            return null;
        }

        if (user.companyUuid() == null) {
            throw new AccessDeniedException("Gestor sem empresa vinculada não pode acessar viagens.");
        }

        return user.companyUuid();
    }
}