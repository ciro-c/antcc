package com.ciroc.antcc.agentes;

import com.ciroc.antcc.ambiente.Posicao;
import com.ciroc.antcc.ambiente.TipoFeromonio;

import java.util.ArrayList;
import java.util.List;

class RetornandoAoNinho extends CicloFormiga {

    private static final double DESVIO = 0.2;

    RetornandoAoNinho(Formiga formiga) {
        super(formiga);
    }

    @Override
    protected void agir() {
        var ambiente = formiga.ambiente();
        Posicao ninho = ambiente.getNinho();

        if (formiga.posicao().equals(ninho)) {
            ambiente.entregarAlimento();
            formiga.setCarregandoAlimento(false);
            concluir(Formiga.CHEGOU_AO_NINHO);
            return;
        }

        formiga.moverPara(proximoPasso(ninho));
        formiga.depositar(TipoFeromonio.ALIMENTO);
    }

    private Posicao proximoPasso(Posicao ninho) {
        if (formiga.aleatorio().nextDouble() < DESVIO) {
            return formiga.vizinhoAleatorio();
        }
        List<Posicao> melhores = new ArrayList<>();
        int menor = Integer.MAX_VALUE;
        for (Posicao v : formiga.ambiente().vizinhos(formiga.posicao())) {
            int d = v.distancia(ninho);
            if (d < menor) {
                menor = d;
                melhores.clear();
            }
            if (d == menor) {
                melhores.add(v);
            }
        }
        return melhores.isEmpty()
                ? formiga.posicao()
                : melhores.get(formiga.aleatorio().nextInt(melhores.size()));
    }
}
