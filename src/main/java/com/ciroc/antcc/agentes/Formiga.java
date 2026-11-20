package com.ciroc.antcc.agentes;

import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.Posicao;
import com.ciroc.antcc.ambiente.TipoFeromonio;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;

import java.util.List;
import java.util.Random;

public class Formiga extends Agent {

    static final String PROCURANDO_ALIMENTO = "procurando-alimento";
    static final String RETORNANDO_AO_NINHO = "retornando-ao-ninho";

    static final int ENCONTROU_ALIMENTO = 1;
    static final int CHEGOU_AO_NINHO = 2;

    private Ambiente ambiente;
    private ParametrosFormiga parametros;
    private Random aleatorio;

    private Posicao posicao;
    private Posicao anterior;
    private boolean carregandoAlimento;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length == 0 || !(args[0] instanceof Ambiente a)) {
            throw new IllegalArgumentException("A formiga precisa receber o Ambiente como primeiro argumento");
        }
        ambiente = a;
        parametros = args.length > 1 && args[1] instanceof ParametrosFormiga p ? p : ParametrosFormiga.padrao();
        aleatorio = parametros.semente() == null
                ? new Random()
                : new Random(parametros.semente() ^ getLocalName().hashCode());

        posicao = ambiente.getNinho();
        if (posicao == null) {
            throw new IllegalStateException("O ambiente não tem ninho definido");
        }
        publicar();

        FSMBehaviour ciclo = new FSMBehaviour(this);
        ciclo.registerFirstState(new ProcurandoAlimento(this), PROCURANDO_ALIMENTO);
        ciclo.registerState(new RetornandoAoNinho(this), RETORNANDO_AO_NINHO);
        ciclo.registerTransition(PROCURANDO_ALIMENTO, RETORNANDO_AO_NINHO, ENCONTROU_ALIMENTO,
                new String[]{RETORNANDO_AO_NINHO});
        ciclo.registerTransition(RETORNANDO_AO_NINHO, PROCURANDO_ALIMENTO, CHEGOU_AO_NINHO,
                new String[]{PROCURANDO_ALIMENTO});
        addBehaviour(ciclo);
    }

    @Override
    protected void takeDown() {
        if (ambiente != null) {
            ambiente.removerFormiga(getLocalName());
        }
    }

    // --- Operações usadas pelos estados -------------------------------------------------------

    Ambiente ambiente() {
        return ambiente;
    }

    ParametrosFormiga parametros() {
        return parametros;
    }

    Random aleatorio() {
        return aleatorio;
    }

    Posicao posicao() {
        return posicao;
    }

    Posicao anterior() {
        return anterior;
    }

    boolean isCarregandoAlimento() {
        return carregandoAlimento;
    }

    void setCarregandoAlimento(boolean carregando) {
        this.carregandoAlimento = carregando;
        publicar();
    }

    void moverPara(Posicao destino) {
        anterior = posicao;
        posicao = destino;
        publicar();
    }

    void depositar(TipoFeromonio tipo) {
        ambiente.depositarFeromonio(posicao, tipo, parametros.depositoFeromonio());
    }

    Posicao vizinhoAleatorio() {
        List<Posicao> vizinhos = ambiente.vizinhos(posicao);
        if (vizinhos.isEmpty()) {
            return posicao;
        }
        if (vizinhos.size() > 1 && anterior != null) {
            vizinhos = vizinhos.stream().filter(v -> !v.equals(anterior)).toList();
        }
        return vizinhos.get(aleatorio.nextInt(vizinhos.size()));
    }

    private void publicar() {
        ambiente.atualizarFormiga(getLocalName(), posicao, carregandoAlimento);
    }
}
