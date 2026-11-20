package com.ciroc.antcc.agentes;

import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.Posicao;
import com.ciroc.antcc.ambiente.TipoCelula;
import com.ciroc.antcc.ambiente.TipoFeromonio;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Formiga extends Agent {

    public enum Estado {
        PROCURANDO_ALIMENTO,
        RETORNANDO_AO_NINHO
    }

    private static final double DESVIO_NO_RETORNO = 0.2;

    private Ambiente ambiente;
    private ParametrosFormiga parametros;
    private Random aleatorio;

    private Estado estado = Estado.PROCURANDO_ALIMENTO;
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

        addBehaviour(new TickerBehaviour(this, parametros.periodoMs()) {
            @Override
            protected void onTick() {
                switch (estado) {
                    case PROCURANDO_ALIMENTO -> procurarAlimento();
                    case RETORNANDO_AO_NINHO -> retornarAoNinho();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        if (ambiente != null) {
            ambiente.removerFormiga(getLocalName());
        }
    }

    private void procurarAlimento() {
        if (ambiente.tipo(posicao.x(), posicao.y()) == TipoCelula.ALIMENTO && ambiente.coletarAlimento(posicao)) {
            carregandoAlimento = true;
            estado = Estado.RETORNANDO_AO_NINHO;
            publicar();
            return;
        }

        moverPara(vizinhoAleatorio());
        depositar(TipoFeromonio.NINHO);
    }

    private void retornarAoNinho() {
        Posicao ninho = ambiente.getNinho();
        if (posicao.equals(ninho)) {
            ambiente.entregarAlimento();
            carregandoAlimento = false;
            estado = Estado.PROCURANDO_ALIMENTO;
            publicar();
            return;
        }

        Posicao proximo = aleatorio.nextDouble() < DESVIO_NO_RETORNO
                ? vizinhoAleatorio()
                : vizinhoMaisPerto(ninho);
        moverPara(proximo);
        depositar(TipoFeromonio.ALIMENTO);
    }

    private void moverPara(Posicao destino) {
        anterior = posicao;
        posicao = destino;
        publicar();
    }

    private void depositar(TipoFeromonio tipo) {
        ambiente.depositarFeromonio(posicao, tipo, parametros.depositoFeromonio());
    }

    private Posicao vizinhoAleatorio() {
        List<Posicao> vizinhos = ambiente.vizinhos(posicao);
        if (vizinhos.isEmpty()) {
            return posicao;
        }
        if (vizinhos.size() > 1 && anterior != null) {
            vizinhos = vizinhos.stream().filter(v -> !v.equals(anterior)).toList();
        }
        return vizinhos.get(aleatorio.nextInt(vizinhos.size()));
    }

    private Posicao vizinhoMaisPerto(Posicao alvo) {
        List<Posicao> melhores = new ArrayList<>();
        int menor = Integer.MAX_VALUE;
        for (Posicao v : ambiente.vizinhos(posicao)) {
            int d = v.distancia(alvo);
            if (d < menor) {
                menor = d;
                melhores.clear();
            }
            if (d == menor) {
                melhores.add(v);
            }
        }
        return melhores.isEmpty() ? posicao : melhores.get(aleatorio.nextInt(melhores.size()));
    }

    private void publicar() {
        ambiente.atualizarFormiga(getLocalName(), posicao, carregandoAlimento);
    }
}
