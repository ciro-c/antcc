package com.ciroc.antcc.ambiente;

/**
 * Coordenada de uma célula na grade. A origem (0, 0) fica no canto inferior esquerdo.
 */
public record Posicao(int x, int y) {

    /** Deslocamentos da vizinhança de Moore (8 vizinhos). */
    static final int[][] DESLOCAMENTOS = {
            {-1, -1}, {0, -1}, {1, -1},
            {-1, 0},           {1, 0},
            {-1, 1},  {0, 1},  {1, 1}
    };

    public Posicao deslocar(int dx, int dy) {
        return new Posicao(x + dx, y + dy);
    }

    /** Distância de Chebyshev, adequada para movimentação em 8 direções. */
    public int distancia(Posicao outra) {
        return Math.max(Math.abs(x - outra.x), Math.abs(y - outra.y));
    }
}
