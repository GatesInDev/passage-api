package br.com.passage.api.iam.enums;

/**
 * Perfis de acesso (RBAC), aplicados pelo Spring Security com o prefixo {@code ROLE_}.
 */
public enum UserRole {

    /** Acesso irrestrito a todos os módulos e empresas. */
    ADMIN("Administrador Geral"),

    /** Gestão de empresa, frota, rotas e viagens da própria empresa. */
    FLEET_MANAGER("Gestor de Frota"),

    /** Venda, alteração e consulta de passagens. */
    TICKET_SELLER("Bilheteiro / Vendedor"),

    /** Consulta de passagens para validação de embarque. */
    INSPECTOR("Fiscal de Bordo / Linha"),

    /** Passageiro: consulta de rotas e viagens. */
    CUSTOMER("Passageiro");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    /** Nome do perfil para exibição. */
    public String getDescription() {
        return description;
    }
}