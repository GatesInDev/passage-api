package br.com.passage.api.integration.internal.service;

import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolve a empresa do usuário autenticado para o módulo fiscal.
 */
@Service
@RequiredArgsConstructor
public class FiscalDocumentAccessService {

    private final IamApi iamApi;

    /**
     * @return {@code null} para administradores, que enxergam todas as
     *         empresas; caso contrário, a empresa do usuário.
     */
    public UUID currentCompanyId() {
        var user = iamApi.currentUser();

        if (user.role() == UserRole.ADMIN) {
            return null;
        }

        if (user.companyUuid() == null) {
            throw new AccessDeniedException(
                    "Consulta de documentos fiscais exige um usuário vinculado a uma empresa.");
        }

        return user.companyUuid();
    }
}
