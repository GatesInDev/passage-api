package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.dto.user.UserResponse;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Serviço responsável pela consulta de usuários.
 *
 * <p>Usuários excluídos logicamente não são retornados.</p>
 */
@Service
@RequiredArgsConstructor
public class FindUserService {

    private final UserRepository userRepository;

    /**
     * Busca um usuário pelo identificador.
     *
     * @param id identificador do usuário
     * @return usuário encontrado
     * @throws ResourceNotFoundException se o usuário não existir ou estiver excluído
     */
    @Transactional(readOnly = true)
    public UserResponse execute(UUID id) {
        User user = userRepository.findById(id)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o identificador: " + id));

        return toResponse(user);
    }

    /**
     * Lista os usuários não excluídos.
     *
     * @return usuários cadastrados
     */
    @Transactional(readOnly = true)
    public List<UserResponse> executeAll() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isDeleted())
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isActive(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompanyUuid()
        );
    }
}