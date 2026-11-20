package com.ciroc.antcc.agentes;

import jade.core.behaviours.TickerBehaviour;

abstract class CicloFormiga extends TickerBehaviour {

    protected final Formiga formiga;
    private int evento;

    CicloFormiga(Formiga formiga) {
        super(formiga, formiga.parametros().periodoMs());
        this.formiga = formiga;
    }

    @Override
    protected final void onTick() {
        agir();
    }

    protected abstract void agir();

    protected void concluir(int evento) {
        this.evento = evento;
        stop();
    }

    @Override
    public int onEnd() {
        return evento;
    }
}
