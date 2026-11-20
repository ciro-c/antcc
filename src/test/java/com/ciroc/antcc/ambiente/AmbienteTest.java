package com.ciroc.antcc.ambiente;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmbienteTest {

    @Test
    void novaGradeComecaVazia() {
        Ambiente a = new Ambiente(5, 4);
        assertEquals(5, a.getLargura());
        assertEquals(4, a.getAltura());
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 4; y++) {
                assertEquals(TipoCelula.VAZIA, a.tipo(x, y));
            }
        }
        assertNull(a.getNinho());
    }

    @Test
    void limitesDaGrade() {
        Ambiente a = new Ambiente(5, 4);
        assertTrue(a.dentroDosLimites(0, 0));
        assertTrue(a.dentroDosLimites(4, 3));
        assertFalse(a.dentroDosLimites(-1, 0));
        assertFalse(a.dentroDosLimites(5, 0));
        assertFalse(a.dentroDosLimites(0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> a.tipo(5, 0));
    }

    @Test
    void definirNinhoMoveONinhoAnterior() {
        Ambiente a = new Ambiente(5, 5);
        a.definirNinho(new Posicao(1, 1));
        a.definirNinho(new Posicao(3, 3));
        assertEquals(new Posicao(3, 3), a.getNinho());
        assertEquals(TipoCelula.VAZIA, a.tipo(1, 1));
        assertEquals(TipoCelula.NINHO, a.tipo(3, 3));
    }

    @Test
    void naoSobrepoeCelulasOcupadas() {
        Ambiente a = new Ambiente(5, 5);
        a.definirNinho(new Posicao(2, 2));
        assertThrows(IllegalStateException.class, () -> a.adicionarFonteAlimento(new Posicao(2, 2), 10));
    }

    @Test
    void coletarAlimentoEsgotaFonte() {
        Ambiente a = new Ambiente(5, 5);
        Posicao fonte = new Posicao(4, 4);
        a.adicionarFonteAlimento(fonte, 2);
        assertEquals(1, a.getFontesAlimento().size());

        assertTrue(a.coletarAlimento(fonte));
        assertEquals(1, a.alimento(fonte));
        assertTrue(a.coletarAlimento(fonte));

        assertFalse(a.coletarAlimento(fonte));
        assertTrue(a.getFontesAlimento().isEmpty());
        assertEquals(TipoCelula.VAZIA, a.tipo(4, 4));
    }

    @Test
    void vizinhosRespeitamBordasEObstaculos() {
        Ambiente a = new Ambiente(5, 5);
        assertEquals(3, a.vizinhos(new Posicao(0, 0)).size());
        assertEquals(8, a.vizinhos(new Posicao(2, 2)).size());

        a.adicionarObstaculo(new Posicao(1, 1));
        assertEquals(2, a.vizinhos(new Posicao(0, 0)).size());
        assertFalse(a.vizinhos(new Posicao(2, 2)).contains(new Posicao(1, 1)));
    }

    @Test
    void cenarioC1TemNinhoEFontes() {
        Ambiente a = Cenarios.c1();
        assertNotNull(a.getNinho());
        assertEquals(2, a.getFontesAlimento().size());
    }
}
