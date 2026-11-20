package com.ciroc.antcc.ambiente;


public class Celula {

    private TipoCelula tipo = TipoCelula.VAZIA;
    private int alimento;

    public TipoCelula getTipo() {
        return tipo;
    }

    void setTipo(TipoCelula tipo) {
        this.tipo = tipo;
        if (tipo != TipoCelula.ALIMENTO) {
            alimento = 0;
        }
    }

    public int getAlimento() {
        return alimento;
    }

    void setAlimento(int alimento) {
        this.alimento = Math.max(0, alimento);
    }

    public boolean isTransitavel() {
        return tipo != TipoCelula.OBSTACULO;
    }
}
