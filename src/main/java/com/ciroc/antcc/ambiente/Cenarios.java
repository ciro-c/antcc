package com.ciroc.antcc.ambiente;

/**
 * Fábrica dos cenários de simulação.
 */
public final class Cenarios {

    public static final int LARGURA_PADRAO = 80;
    public static final int ALTURA_PADRAO = 60;
    public static final int ALIMENTO_POR_FONTE = 500;

    private Cenarios() {
    }

    /**
     * Cenário C1: ninho próximo a um canto e duas fontes de alimento afastadas, sem obstáculos.
     */
    public static Ambiente c1() {
        Ambiente ambiente = new Ambiente(LARGURA_PADRAO, ALTURA_PADRAO);
        ambiente.definirNinho(new Posicao(10, 10));
        ambiente.adicionarFonteAlimento(new Posicao(65, 45), ALIMENTO_POR_FONTE);
        ambiente.adicionarFonteAlimento(new Posicao(60, 15), ALIMENTO_POR_FONTE);
        return ambiente;
    }
}
