package com.ciroc.antcc.agentes;

import com.ciroc.antcc.ambiente.TipoCelula;
import com.ciroc.antcc.ambiente.TipoFeromonio;

class ProcurandoAlimento extends CicloFormiga {

    ProcurandoAlimento(Formiga formiga) {
        super(formiga);
    }

    @Override
    protected void agir() {
        var ambiente = formiga.ambiente();
        var aqui = formiga.posicao();

        if (ambiente.tipo(aqui.x(), aqui.y()) == TipoCelula.ALIMENTO && ambiente.coletarAlimento(aqui)) {
            formiga.setCarregandoAlimento(true);
            concluir(Formiga.ENCONTROU_ALIMENTO);
            return;
        }

        formiga.moverPara(formiga.vizinhoAleatorio());
        formiga.depositar(TipoFeromonio.NINHO);
    }
}
