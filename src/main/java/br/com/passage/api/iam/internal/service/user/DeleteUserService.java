package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteUserService {

    private final UserRepository userRepository;

    @Transactional
    public void execute(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o identificador: " + id));

        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }
}