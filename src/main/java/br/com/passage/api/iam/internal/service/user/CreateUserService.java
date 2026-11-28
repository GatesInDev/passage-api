package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.dto.user.CreateUserRequest;
import br.com.passage.api.iam.dto.user.UserResponse;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo cadastro de usuários.
 *
 * <p>O e-mail deve ser único no sistema e a senha é armazenada como hash BCrypt.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Cadastra um usuário.
     *
     * @param request dados do usuário
     * @return usuário cadastrado
     * @throws BusinessException se o e-mail já estiver em uso
     */
    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = new User(
                request.name(),
                request.email(),
                encodedPassword,
                request.role(),
                request.companyUuid()
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.isActive(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCompanyUuid()
        );
    }
}