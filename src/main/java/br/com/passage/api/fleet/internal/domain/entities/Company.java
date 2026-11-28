package br.com.passage.api.fleet.internal.domain.entities;

import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.br.CNPJ;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma empresa cadastrada na plataforma.
 *
 * <p>A empresa contém as informações cadastrais necessárias para sua
 * identificação e relacionamento com os demais recursos da aplicação,
 * especialmente aqueles relacionados à operação de frota.</p>
 *
 * <p>Herda de {@link EntityBase} os atributos comuns de identificação,
 * auditoria, controle de estado, exclusão lógica e controle de
 * concorrência.</p>
 *
 * <h2>Dados cadastrais</h2>
 *
 * <ul>
 *     <li>
 *         {@link #corporateName} — razão social da empresa. Campo obrigatório.
 *     </li>
 *     <li>
 *         {@link #tradeName} — nome fantasia da empresa. Campo opcional.
 *     </li>
 *     <li>
 *         {@link #documentId} — CNPJ da empresa. Campo obrigatório e
 *         validado através da anotação {@link CNPJ}.
 *     </li>
 *     <li>
 *         {@link #stateRegistration} — inscrição estadual da empresa.
 *         Campo obrigatório.
 *     </li>
 *     <li>
 *         {@link #stateRegulatoryAgency} — órgão regulador estadual
 *         relacionado à empresa. Campo obrigatório.
 *     </li>
 * </ul>
 *
 * <h2>Persistência</h2>
 *
 * <p>A entidade é persistida na tabela {@code company} e utiliza UUID,
 * auditoria, exclusão lógica e controle de concorrência herdados de
 * {@link EntityBase}.</p>
 *
 * @see EntityBase
 * @see CNPJ
 *
 * @author Vitor Hugo Altmann <contato@vitoraltmann.dev>
 */
@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Company extends EntityBase {

    /**
     * Razão social da empresa.
     *
     * <p>Nome empresarial registrado oficialmente. É obrigatório
     * e possui limite de 255 caracteres.</p>
     */
    @Column(name = "corporate_name", nullable = false, length = 255)
    private String corporateName;

    /**
     * Nome fantasia da empresa.
     *
     * <p>Nome comercial utilizado pela empresa. É opcional e possui
     * limite de 255 caracteres.</p>
     */
    @Column(name = "trade_name", length = 255)
    private String tradeName;

    /**
     * CNPJ da empresa.
     *
     * <p>Identificador fiscal da pessoa jurídica no Brasil.
     * O valor é validado através da anotação {@link CNPJ}.</p>
     *
     * <p>O tamanho máximo armazenado é de 14 caracteres, permitindo
     * a representação do CNPJ alfanumérico.</p>
     */
    @CNPJ(format = CNPJ.Format.ALPHANUMERIC)
    @Column(name = "document_id", nullable = false, length = 14, unique = true)
    private String documentId;

    /**
     * Inscrição estadual da empresa.
     *
     * <p>Identificação fiscal estadual vinculada à empresa.
     * É um campo obrigatório e possui limite de 15 caracteres.</p>
     */
    @Column(name = "state_registration", nullable = false, length = 15)
    private String stateRegistration;

    /**
     * Órgão regulador estadual relacionado à empresa.
     *
     * <p>Identifica o órgão ou entidade responsável pela regulamentação
     * estadual aplicável à empresa.</p>
     */
    @Column(name = "state_regulatory_agency", nullable = false, length = 255)
    private String stateRegulatoryAgency;

    /**
     * Lista de motoristas vinculados à empresa transportadora.
     * <p>
     * Configurada com deleção em cascata e remoção de órfãos.
     * </p>
     */
    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Driver> drivers = new ArrayList<>();

    /**
     * Adiciona um motorista à empresa e sincroniza a relação bidirecional JPA.
     *
     * @param driver Instância do {@link Driver} a ser vinculado a esta empresa.
     */
    public void addDriver(Driver driver) {
        drivers.add(driver);
        driver.setCompany(this);
    }

    /**
     * Remove o motorista da empresa e desfaz o vínculo da relação bidirecional JPA.
     *
     * @param driver Instância do {@link Driver} a ser desvinculado desta empresa.
     */
    public void removeDriver(Driver driver) {
        drivers.remove(driver);
        driver.setCompany(null);
    }
}
