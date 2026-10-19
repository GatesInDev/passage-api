package br.com.passage.api.iam.internal.domain.enums;

/**
 * Define os papéis e níveis de autoridade de acesso dos usuários no sistema Passage API.
 * <p>
 * Utilizado para controle de autorização baseado em papéis (RBAC - Role-Based Access Control)
 * integrado aos mecanismos de segurança do Spring Security.
 * </p>
 */
public enum UserRole {

    /**
     * Acesso irrestrito a todas as configurações, módulos e recursos da plataforma.
     */
    ADMIN("Administrador Geral"),

    /**
     * Gestão de frotas, linhas, escalas de veículos, motoristas e manutenções.
     */
    FLEET_MANAGER("Gestor de Frota"),

    /**
     * Operação de bilheteria: emissão, cancelamento, revalidação e consulta de passagens.
     */
    TICKET_SELLER("Bilheteiro / Vendedor"),

    /**
     * Validação de embarques, leitura de QR codes de bilhetes e conferência de bagagens.
     */
    INSPECTOR("Fiscal de Bordo / Linha"),

    /**
     * Passageiro final: consulta de viagens e compra/visualização de seus próprios bilhetes.
     */
    CUSTOMER("Passageiro");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    /**
     * Recupera o descritivo amigável do papel do usuário.
     *
     * @return Descrição legível do perfil.
     */
    public String getDescription() {
        return description;
    }
}