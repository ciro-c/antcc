package com.ciroc.antcc.agentes;

import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.Posicao;
import com.ciroc.antcc.ambiente.TipoFeromonio;
import com.ciroc.antcc.plataforma.PlataformaJade;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FormigaTest {

    @Test
    void formigaBuscaAlimentoEVoltaAoNinhoDepositandoFeromonio() throws InterruptedException {
        Ambiente ambiente = new Ambiente(6, 6);
        ambiente.definirNinho(new Posicao(0, 0));
        ambiente.adicionarFonteAlimento(new Posicao(4, 4), 100);

        try (PlataformaJade plataforma = new PlataformaJade(false)) {
            plataforma.iniciarAgente("formiga-teste", Formiga.class,
                    ambiente, new ParametrosFormiga(1, 10f, 0.97f, 2.0, 42L));

            long limite = System.currentTimeMillis() + 20_000;
            while (ambiente.getAlimentoNoNinho() < 2 && System.currentTimeMillis() < limite) {
                Thread.sleep(20);
            }
        }

        assertTrue(ambiente.getAlimentoNoNinho() >= 2, "a formiga deveria completar ao menos dois ciclos");
        assertTrue(ambiente.alimento(new Posicao(4, 4)) <= 98);

        float[][][] f = ambiente.instantaneoFeromonio();
        assertTrue(soma(f[TipoFeromonio.NINHO.ordinal()]) > 0);
        assertTrue(soma(f[TipoFeromonio.ALIMENTO.ordinal()]) > 0);
    }

    @Test
    void sorteioFavoreceCelulasComMaisFeromonio() {
        Random aleatorio = new Random(7);
        double[] pesos = {1, 1, 98};
        int[] contagem = new int[3];
        for (int i = 0; i < 10_000; i++) {
            contagem[Formiga.sortear(pesos, aleatorio)]++;
        }
        assertTrue(contagem[2] > 9_500, "a célula com mais feromônio deveria ser escolhida quase sempre");
        assertTrue(contagem[0] > 0 && contagem[1] > 0, "as demais ainda devem ter chance de ser escolhidas");
    }

    @Test
    void sorteioComPesosIguaisEUniforme() {
        Random aleatorio = new Random(7);
        double[] pesos = {1, 1, 1, 1};
        int[] contagem = new int[4];
        for (int i = 0; i < 40_000; i++) {
            contagem[Formiga.sortear(pesos, aleatorio)]++;
        }
        for (int c : contagem) {
            assertEquals(10_000, c, 500);
        }
    }

    private static float soma(float[][] camada) {
        float total = 0;
        for (float[] coluna : camada) {
            for (float v : coluna) {
                total += v;
            }
        }
        return total;
    }
}
