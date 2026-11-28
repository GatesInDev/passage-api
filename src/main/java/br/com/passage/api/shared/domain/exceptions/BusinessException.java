package br.com.passage.api.shared.domain.exceptions;

/**
 * Representa a violação de uma regra de negócio.
 *
 * <p>É lançada quando a operação é válida no formato, mas conflita com o estado atual dos dados,
 * como um código já cadastrado ou uma transição de situação não permitida.</p>
 *
 * <p>É convertida pelo {@link GlobalExceptionHandler} em uma resposta {@code 409 Conflict}.</p>
 *
 * @see GlobalExceptionHandler
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}