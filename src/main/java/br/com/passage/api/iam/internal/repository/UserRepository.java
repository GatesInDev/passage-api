package br.com.passage.api.iam.internal.repository;

import br.com.passage.api.iam.internal.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<UserDetails> findByEmail(String email);
    Optional<User> findById(UUID id);
    boolean existsByEmail(String email);
}