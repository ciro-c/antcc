package com.ciroc.antcc.agentes;

import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.Posicao;
import com.ciroc.antcc.ambiente.TipoFeromonio;
import com.ciroc.antcc.plataforma.PlataformaJade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormigaTest {

    @Test
    void formigaBuscaAlimentoEVoltaAoNinhoDepositandoFeromonio() throws InterruptedException {
        Ambiente ambiente = new Ambiente(6, 6);
        ambiente.definirNinho(new Posicao(0, 0));
        ambiente.adicionarFonteAlimento(new Posicao(4, 4), 100);

        try (PlataformaJade plataforma = new PlataformaJade(false)) {
            plataforma.iniciarAgente("formiga-teste", Formiga.class,
                    ambiente, new ParametrosFormiga(1, 1f, 42L));

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
