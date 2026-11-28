package br.com.passage.api.fleet.internal.domain.enums;

/**
 * Representa a posição física do assento dentro da fileira.
 */
public enum SeatPosition {

    /**
     * Assento localizado junto à janela.
     */
    WINDOW,

    /**
     * Assento localizado junto ao corredor.
     */
    AISLE,

    /**
     * Assento localizado entre outros assentos.
     */
    MIDDLE
}