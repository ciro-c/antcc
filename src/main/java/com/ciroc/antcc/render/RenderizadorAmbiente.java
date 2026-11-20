package com.ciroc.antcc.render;

import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.TipoCelula;

import static org.lwjgl.opengl.GL11.*;

public class RenderizadorAmbiente implements Renderizador {

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

        glBegin(GL_QUADS);
        for (int x = 0; x < colunas; x++) {
            for (int y = 0; y < linhas; y++) {
                cor(tipos[x][y]);
                glVertex2f(x, y);
                glVertex2f(x + 1, y);
                glVertex2f(x + 1, y + 1);
                glVertex2f(x, y + 1);
            }
        }
        glEnd();
    }

    private static void cor(TipoCelula tipo) {
        switch (tipo) {
            case NINHO -> glColor3f(0.55f, 0.30f, 0.10f);
            case ALIMENTO -> glColor3f(0.20f, 0.75f, 0.25f);
            case OBSTACULO -> glColor3f(0.45f, 0.45f, 0.50f);
            case VAZIA -> glColor3f(0.93f, 0.89f, 0.80f);
        }
    }
}
