package br.com.passage.api.shared.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Classe base para todas as entidades persistentes da aplicação.
 *
 * <p>Centraliza os atributos comuns de identificação, auditoria, controle
 * de estado e concorrência. As entidades de domínio devem estender esta
 * classe para herdar esses comportamentos.</p>
 *
 * <h2>Campos herdados</h2>
 *
 * <ul>
 *     <li>
 *         {@link #id} — identificador único da entidade, gerado automaticamente
 *         como {@link UUID}. É protegido contra alterações após a persistência.
 *     </li>
 *     <li>
 *         {@link #createdAt} — data e hora de criação do registro, preenchida
 *         automaticamente pelo Spring Data JPA.
 *     </li>
 *     <li>
 *         {@link #updatedAt} — data e hora da última atualização do registro,
 *         preenchida automaticamente pelo Spring Data JPA.
 *     </li>
 *     <li>
 *         {@link #isActive} — indica se o registro está ativo para utilização
 *         pelas regras de negócio.
 *     </li>
 *     <li>
 *         {@link #isDeleted} — indica se o registro foi excluído logicamente.
 *         A exclusão lógica mantém o registro fisicamente armazenado no banco.
 *     </li>
 *     <li>
 *         {@link #version} — versão utilizada pelo JPA para controle de
 *         concorrência otimista.
 *     </li>
 * </ul>
 *
 * <h2>Encapsulamento</h2>
 *
 * <p>Os atributos possuem visibilidade {@code protected}, permitindo que as
 * entidades filhas tenham acesso direto aos campos quando necessário, sem
 * expô-los diretamente para outras classes do domínio.</p>
 *
 * <p>A classe fornece acesso de leitura através dos getters gerados pelo
 * Lombok. A criação de instâncias é realizada preferencialmente através do
 * {@link SuperBuilder} disponibilizado pelas entidades concretas.</p>
 *
 * <h2>Auditoria</h2>
 *
 * <p>A classe utiliza {@link AuditingEntityListener} para preencher
 * automaticamente os campos {@link #createdAt} e {@link #updatedAt}.
 * Para que esse comportamento funcione, o auditing do Spring Data JPA
 * deve estar habilitado na aplicação.</p>
 *
 * <h2>Concorrência</h2>
 *
 * <p>O campo {@link #version} utiliza {@link Version} para implementar
 * controle de concorrência otimista. Alterações concorrentes no mesmo
 * registro podem resultar em uma exceção de concorrência do JPA.</p>
 *
 * <h2>Exclusão</h2>
 *
 * <p>A propriedade {@link #isDeleted} representa exclusão lógica.
 * Esta classe não implementa automaticamente filtros para registros
 * excluídos; essa responsabilidade deve ser definida pelas consultas
 * ou pela camada de persistência.</p>
 *
 * @see MappedSuperclass
 * @see AuditingEntityListener
 * @see Version
 * @see CreatedDate
 * @see LastModifiedDate
 *
 * @author Vitor Hugo Altmann <contato@vitoraltmann.dev>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntityBase {

    /**
     * Identificador único da entidade.
     *
     * <p>Gerado automaticamente pelo JPA utilizando UUID.
     * O campo não pode ser alterado após a persistência.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    protected UUID id;

    /**
     * Data e hora de criação do registro.
     *
     * <p>Preenchida automaticamente pelo Spring Data JPA.</p>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    /**
     * Data e hora da última atualização do registro.
     *
     * <p>Atualizada automaticamente pelo Spring Data JPA sempre que
     * a entidade for modificada.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    /**
     * Indica se a entidade está ativa.
     *
     * <p>Por padrão, representa o estado de disponibilidade do registro
     * para utilização pelas regras de negócio.</p>
     */
    @Setter
    @Column(name = "is_active", nullable = false)
    protected boolean isActive = true;

    /**
     * Indica se a entidade foi excluída logicamente.
     *
     * <p>Quando {@code true}, o registro permanece fisicamente armazenado
     * no banco de dados, mas é considerado excluído pela aplicação.</p>
     */
    @Setter
    @Column(name = "is_deleted", nullable = false)
    protected boolean isDeleted = false;

    /**
     * Versão do registro para controle de concorrência otimista.
     *
     * <p>Gerenciada automaticamente pelo JPA e incrementada a cada
     * atualização persistida.</p>
     */
    @Version
    @Column(name = "version", nullable = false)
    protected Long version;
}
