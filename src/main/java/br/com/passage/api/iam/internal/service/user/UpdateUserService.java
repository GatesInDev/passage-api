package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.dto.user.UpdateUserRequest;
import br.com.passage.api.iam.dto.user.UserResponse;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela atualização de usuários.
 *
 * <p>Permite alterar o nome, o perfil, a empresa vinculada e a situação do usuário.
 * O e-mail e a senha não são alterados por este serviço.</p>
 */
@Service
@RequiredArgsConstructor
public class UpdateUserService {

    private final UserRepository userRepository;

    /**
     * Atualiza um usuário.
     *
     * @param id      identificador do usuário
     * @param request novos dados do usuário
     * @return usuário atualizado
     * @throws ResourceNotFoundException se o usuário não existir ou estiver excluído
     */
    @Transactional
    public UserResponse execute(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o identificador: " + id));

        user.setName(request.name());
        user.setRole(request.role());
        user.setCompanyUuid(request.companyUuid());
        user.setActive(request.isActive());

        User updatedUser = userRepository.save(user);

        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt(),
                updatedUser.isActive(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getCompanyUuid()
        );
    }
}