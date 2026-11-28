package br.com.passage.api.integration.internal.service;

import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Define o escopo de empresa das consultas de documentos fiscais para o usuário logado. */
@Service
@RequiredArgsConstructor
public class FiscalDocumentAccessService {

    private final IamApi iamApi;

    /**
     * @return a empresa do usuário, ou {@code null} (sem restrição) para o ADMIN
     * @throws AccessDeniedException se o usuário não for ADMIN nem tiver empresa vinculada
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
