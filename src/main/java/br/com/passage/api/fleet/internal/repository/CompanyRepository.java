package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

/** Acesso às empresas. */
public interface CompanyRepository extends JpaRepository<Company, UUID>
{
    boolean existsByDocumentId(String documentId);
    boolean existsByDocumentIdAndIdNot(String documentId, UUID id);
    Optional<Company> findByIdAndIsDeletedFalse(UUID id);
    List<Company> findAllByIsDeletedFalse();
    /** Lista com no máximo uma empresa; usada para o gestor ver só a própria. */
    List<Company> findAllByIdAndIsDeletedFalse(UUID id);
}
