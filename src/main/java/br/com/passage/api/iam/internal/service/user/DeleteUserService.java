package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela exclusão de usuários.
 *
 * <p>A exclusão é lógica: o usuário é marcado como excluído e inativo, permanece no banco
 * e deixa de conseguir se autenticar.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteUserService {

    private final UserRepository userRepository;

    /**
     * Exclui logicamente um usuário.
     *
     * @param id identificador do usuário
     * @throws ResourceNotFoundException se o usuário não existir ou já estiver excluído
     */
    @Transactional
    public void execute(UUID id) {
        User user = userRepository.findById(id)
                .filter(existingUser -> !existingUser.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o identificador: " + id));

        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }
}