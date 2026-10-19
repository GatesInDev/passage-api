package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.dto.user.UpdateUserRequest;
import br.com.passage.api.iam.dto.user.UserResponse;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse execute(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o identificador: " + id));

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