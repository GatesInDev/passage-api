package br.com.passage.api.shared.domain.exceptions;

/**
 * Representa a ausência de um recurso solicitado.
 *
 * <p>É lançada quando o registro não existe, foi excluído logicamente ou pertence a uma empresa
 * fora do escopo do usuário autenticado.</p>
 *
 * <p>É convertida pelo {@link GlobalExceptionHandler} em uma resposta {@code 404 Not Found}.</p>
 *
 * @see GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
