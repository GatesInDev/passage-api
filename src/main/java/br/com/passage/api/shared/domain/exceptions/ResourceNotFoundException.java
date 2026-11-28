package br.com.passage.api.shared.domain.exceptions;

/**
 * Recurso inexistente, excluído ou fora do escopo do usuário, respondido como 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
