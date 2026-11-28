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
 * Base de todas as entidades: identificador, auditoria, exclusão lógica e versão.
 *
 * <p>A exclusão é lógica: o registro permanece no banco com {@link #isDeleted}
 * marcado, e cabe às consultas de cada módulo desconsiderá-lo.</p>
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

    /** Identificador público, gerado na persistência e imutável. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    protected UUID id;

    /** Momento da criação, preenchido pela auditoria do JPA. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    /** Momento da última alteração, preenchido pela auditoria do JPA. */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    /** Se o registro está disponível para uso nas regras de negócio. */
    @Setter
    @Column(name = "is_active", nullable = false)
    protected boolean isActive = true;

    /** Exclusão lógica: o registro continua no banco, mas deixa de existir para a API. */
    @Setter
    @Column(name = "is_deleted", nullable = false)
    protected boolean isDeleted = false;

    /** Controle de concorrência otimista, incrementado a cada alteração. */
    @Version
    @Column(name = "version", nullable = false)
    protected Long version;
}
