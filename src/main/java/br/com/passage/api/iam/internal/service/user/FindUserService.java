package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.dto.user.UserResponse;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse execute(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o identificador: " + id));

        return toResponse(user);
    }

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