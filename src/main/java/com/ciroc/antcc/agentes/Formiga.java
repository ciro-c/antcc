package com.ciroc.antcc.agentes;

import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.Posicao;
import com.ciroc.antcc.ambiente.TipoCelula;
import com.ciroc.antcc.ambiente.TipoFeromonio;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.List;
import java.util.Random;

public class Formiga extends Agent {

    public enum Estado {
        PROCURANDO_ALIMENTO,
        RETORNANDO_AO_NINHO
    }

    private static final double FEROMONIO_BASE = 1.0;
    private static final double PERSISTENCIA = 1.5;

    private Ambiente ambiente;
    private ParametrosFormiga parametros;
    private Random aleatorio;

    private Estado estado = Estado.PROCURANDO_ALIMENTO;
    private Posicao posicao;
    private Posicao anterior;
    private boolean carregandoAlimento;
    private int passosDesdeOrigem;

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
            passosDesdeOrigem = 0;
            anterior = null;
            publicar();
            return;
        }

        Posicao alimento = vizinhoDoTipo(TipoCelula.ALIMENTO);
        moverPara(alimento != null ? alimento : escolherDirecao(TipoFeromonio.ALIMENTO, rumoAtual()));
        depositar(TipoFeromonio.NINHO);
    }

    private void retornarAoNinho() {
        Posicao ninho = ambiente.getNinho();
        if (posicao.equals(ninho)) {
            ambiente.entregarAlimento();
            carregandoAlimento = false;
            estado = Estado.PROCURANDO_ALIMENTO;
            passosDesdeOrigem = 0;
            anterior = null;
            publicar();
            return;
        }

        Posicao proximo;
        if (posicao.distancia(ninho) == 1 && ambiente.isTransitavel(ninho)) {
            proximo = ninho;
        } else {
            proximo = escolherDirecao(TipoFeromonio.NINHO, rumoPara(ninho));
        }
        moverPara(proximo);
        depositar(TipoFeromonio.ALIMENTO);
    }

    private Posicao escolherDirecao(TipoFeromonio guia, double[] rumo) {
        List<Posicao> candidatos = candidatos();
        if (candidatos.isEmpty()) {
            return posicao;
        }
        double[] pesos = new double[candidatos.size()];
        for (int i = 0; i < pesos.length; i++) {
            Posicao v = candidatos.get(i);
            double concentracao = ambiente.feromonio(v, guia);
            pesos[i] = Math.pow(FEROMONIO_BASE + concentracao, parametros.influenciaFeromonio())
                    * Math.exp(PERSISTENCIA * alinhamento(v, rumo));
        }
        return candidatos.get(sortear(pesos, aleatorio));
    }

    private double[] rumoAtual() {
        return anterior == null
                ? new double[]{0, 0}
                : new double[]{posicao.x() - anterior.x(), posicao.y() - anterior.y()};
    }

    private double[] rumoPara(Posicao alvo) {
        return new double[]{alvo.x() - posicao.x(), alvo.y() - posicao.y()};
    }

    private double alinhamento(Posicao vizinho, double[] rumo) {
        double rx = rumo[0], ry = rumo[1];
        double mx = vizinho.x() - posicao.x(), my = vizinho.y() - posicao.y();
        double normas = Math.hypot(rx, ry) * Math.hypot(mx, my);
        return normas == 0 ? 0 : (rx * mx + ry * my) / normas;
    }

    static int sortear(double[] pesos, Random aleatorio) {
        double total = 0;
        for (double p : pesos) {
            total += p;
        }
        double alvo = aleatorio.nextDouble() * total;
        for (int i = 0; i < pesos.length; i++) {
            alvo -= pesos[i];
            if (alvo < 0) {
                return i;
            }
        }
        return pesos.length - 1;
    }

    private Posicao vizinhoDoTipo(TipoCelula tipo) {
        for (Posicao v : ambiente.vizinhos(posicao)) {
            if (ambiente.tipo(v.x(), v.y()) == tipo) {
                return v;
            }
        }
        return null;
    }

    private List<Posicao> candidatos() {
        List<Posicao> vizinhos = ambiente.vizinhos(posicao);
        if (anterior == null) {
            return vizinhos;
        }
        int dx = posicao.x() - anterior.x();
        int dy = posicao.y() - anterior.y();
        List<Posicao> aFrente = vizinhos.stream()
                .filter(v -> (v.x() - posicao.x()) * dx + (v.y() - posicao.y()) * dy >= 0)
                .toList();
        return aFrente.isEmpty() ? vizinhos : aFrente;
    }

    private void moverPara(Posicao destino) {
        anterior = posicao;
        posicao = destino;
        passosDesdeOrigem++;
        publicar();
    }

    private void depositar(TipoFeromonio tipo) {
        float quantidade = (float) (parametros.depositoFeromonio()
                * Math.pow(parametros.decaimentoDeposito(), passosDesdeOrigem));
        ambiente.depositarFeromonio(posicao, tipo, quantidade);
    }

    private void publicar() {
        ambiente.atualizarFormiga(getLocalName(), posicao, carregandoAlimento);
    }
}
