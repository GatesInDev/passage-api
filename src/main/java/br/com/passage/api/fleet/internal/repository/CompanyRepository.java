package br.com.passage.api.fleet.internal.repository;

import br.com.passage.api.fleet.internal.domain.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

/**
 * Repositório de acesso às empresas.
 */
public interface CompanyRepository extends JpaRepository<Company, UUID>
{
    /**
     * Verifica se já existe uma empresa cadastrada com o CNPJ informado.
     *
     * <p>Considera também as empresas excluídas logicamente.</p>
     *
     * @param documentId CNPJ da empresa
     * @return {@code true} se o CNPJ já estiver em uso
     */
    boolean existsByDocumentId(String documentId);
    /**
     * Verifica se o CNPJ informado já está em uso por outra empresa.
     *
     * @param documentId CNPJ da empresa
     * @param id         empresa desconsiderada na verificação
     * @return {@code true} se o CNPJ já estiver em uso por outra empresa
     */
    boolean existsByDocumentIdAndIdNot(String documentId, UUID id);
    /**
     * Busca uma empresa não excluída pelo identificador.
     *
     * @param id identificador da empresa
     * @return empresa encontrada, se existir
     */
    Optional<Company> findByIdAndIsDeletedFalse(UUID id);
    /**
     * Lista as empresas não excluídas.
     *
     * @return empresas encontradas
     */
    List<Company> findAllByIsDeletedFalse();
    /**
     * Lista a empresa informada, caso não esteja excluída.
     *
     * <p>Utilizada na listagem do gestor de frota, que enxerga apenas a própria empresa.</p>
     *
     * @param id identificador da empresa
     * @return lista com a empresa, ou vazia
     */
    List<Company> findAllByIdAndIsDeletedFalse(UUID id);
}
