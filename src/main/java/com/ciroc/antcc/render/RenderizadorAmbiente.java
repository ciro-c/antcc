package com.ciroc.antcc.render;

import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.PosicaoFormiga;
import com.ciroc.antcc.ambiente.TipoCelula;
import com.ciroc.antcc.ambiente.TipoFeromonio;

import static org.lwjgl.opengl.GL11.*;

public class RenderizadorAmbiente implements Renderizador {

    private static final float[] COR_FUNDO = {0.93f, 0.89f, 0.80f};
    private static final float[] COR_FEROMONIO_NINHO = {0.15f, 0.35f, 0.90f};
    private static final float[] COR_FEROMONIO_ALIMENTO = {0.90f, 0.15f, 0.15f};

    private static final float SATURACAO = TipoFeromonio.MAXIMO / 4;

    private final Ambiente ambiente;

    public RenderizadorAmbiente(Ambiente ambiente) {
        this.ambiente = ambiente;
    }

    @Override
    public void desenhar(int larguraFramebuffer, int alturaFramebuffer) {
        int colunas = ambiente.getLargura();
        int linhas = ambiente.getAltura();

        // Projeção em coordenadas de grade, com margem para preservar a proporção.
        double escala = Math.min((double) larguraFramebuffer / colunas, (double) alturaFramebuffer / linhas);
        double sobraX = (larguraFramebuffer / escala - colunas) / 2;
        double sobraY = (alturaFramebuffer / escala - linhas) / 2;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-sobraX, colunas + sobraX, -sobraY, linhas + sobraY, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        TipoCelula[][] tipos = ambiente.instantaneo();
        float[][][] feromonio = ambiente.instantaneoFeromonio();
        float[][] ninho = feromonio[TipoFeromonio.NINHO.ordinal()];
        float[][] alimento = feromonio[TipoFeromonio.ALIMENTO.ordinal()];

        glBegin(GL_QUADS);
        for (int x = 0; x < colunas; x++) {
            for (int y = 0; y < linhas; y++) {
                if (tipos[x][y] == TipoCelula.VAZIA) {
                    corFeromonio(ninho[x][y], alimento[x][y]);
                } else {
                    cor(tipos[x][y]);
                }
                quadrado(x, y, 0f);
            }
        }

        for (PosicaoFormiga f : ambiente.getFormigas()) {
            if (f.carregandoAlimento()) {
                glColor3f(0.10f, 0.45f, 0.10f);
            } else {
                glColor3f(0.10f, 0.08f, 0.06f);
            }
            quadrado(f.posicao().x(), f.posicao().y(), 0.2f);
        }
        glEnd();
    }

    private static void quadrado(float x, float y, float margem) {
        glVertex2f(x + margem, y + margem);
        glVertex2f(x + 1 - margem, y + margem);
        glVertex2f(x + 1 - margem, y + 1 - margem);
        glVertex2f(x + margem, y + 1 - margem);
    }

    private static void corFeromonio(float concentracaoNinho, float concentracaoAlimento) {
        float iN = Math.min(1f, concentracaoNinho / SATURACAO);
        float iA = Math.min(1f, concentracaoAlimento / SATURACAO);
        float soma = iN + iA;
        if (soma == 0f) {
            glColor3f(COR_FUNDO[0], COR_FUNDO[1], COR_FUNDO[2]);
            return;
        }
        // Cor combinada ponderada pelas concentrações; a intensidade é a do tipo mais forte.
        float intensidade = Math.max(iN, iA);
        float[] c = new float[3];
        for (int i = 0; i < 3; i++) {
            float tinta = (COR_FEROMONIO_NINHO[i] * iN + COR_FEROMONIO_ALIMENTO[i] * iA) / soma;
            c[i] = COR_FUNDO[i] + (tinta - COR_FUNDO[i]) * intensidade;
        }
        glColor3f(c[0], c[1], c[2]);
    }

    private static void cor(TipoCelula tipo) {
        switch (tipo) {
            case NINHO -> glColor3f(0.55f, 0.30f, 0.10f);
            case ALIMENTO -> glColor3f(0.20f, 0.75f, 0.25f);
            case OBSTACULO -> glColor3f(0.45f, 0.45f, 0.50f);
            case VAZIA -> glColor3f(COR_FUNDO[0], COR_FUNDO[1], COR_FUNDO[2]);
        }
    }
}
