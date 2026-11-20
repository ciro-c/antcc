package com.ciroc.antcc.ambiente;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Ambiente {

    private final int largura;
    private final int altura;
    private final Celula[][] celulas;
    private final List<Posicao> fontesAlimento = new ArrayList<>();
    private final float[][][] feromonio;
    private final Map<String, PosicaoFormiga> formigas = new LinkedHashMap<>();
    private Posicao ninho;
    private int alimentoNoNinho;

    public Ambiente(int largura, int altura) {
        if (largura <= 0 || altura <= 0) {
            throw new IllegalArgumentException("Dimensões da grade devem ser positivas");
        }
        this.largura = largura;
        this.altura = altura;
        this.celulas = new Celula[largura][altura];
        this.feromonio = new float[TipoFeromonio.values().length][largura][altura];
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                celulas[x][y] = new Celula();
            }
        }
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    public boolean dentroDosLimites(int x, int y) {
        return x >= 0 && y >= 0 && x < largura && y < altura;
    }

    public boolean dentroDosLimites(Posicao p) {
        return dentroDosLimites(p.x(), p.y());
    }

    public synchronized TipoCelula tipo(int x, int y) {
        return celula(x, y).getTipo();
    }

    public synchronized int alimento(Posicao p) {
        return celula(p.x(), p.y()).getAlimento();
    }

    public synchronized Posicao getNinho() {
        return ninho;
    }

    public synchronized void definirNinho(Posicao p) {
        Celula c = celulaLivre(p);
        if (ninho != null) {
            celula(ninho.x(), ninho.y()).setTipo(TipoCelula.VAZIA);
        }
        c.setTipo(TipoCelula.NINHO);
        ninho = p;
    }

    public synchronized void adicionarFonteAlimento(Posicao p, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade de alimento deve ser positiva");
        }
        Celula c = celulaLivre(p);
        c.setTipo(TipoCelula.ALIMENTO);
        c.setAlimento(quantidade);
        fontesAlimento.add(p);
    }

    public synchronized void removerFonteAlimento(Posicao p) {
        if (fontesAlimento.remove(p)) {
            celula(p.x(), p.y()).setTipo(TipoCelula.VAZIA);
        }
    }

 
    public synchronized boolean coletarAlimento(Posicao p) {
        Celula c = celula(p.x(), p.y());
        if (c.getTipo() != TipoCelula.ALIMENTO || c.getAlimento() == 0) {
            return false;
        }
        c.setAlimento(c.getAlimento() - 1);
        if (c.getAlimento() == 0) {
            removerFonteAlimento(p);
        }
        return true;
    }

    public synchronized List<Posicao> getFontesAlimento() {
        return Collections.unmodifiableList(new ArrayList<>(fontesAlimento));
    }

    public synchronized void adicionarObstaculo(Posicao p) {
        celulaLivre(p).setTipo(TipoCelula.OBSTACULO);
    }

    public synchronized boolean isTransitavel(Posicao p) {
        return dentroDosLimites(p) && celula(p.x(), p.y()).isTransitavel();
    }

    public synchronized List<Posicao> vizinhos(Posicao p) {
        List<Posicao> vizinhos = new ArrayList<>(8);
        for (int[] d : Posicao.DESLOCAMENTOS) {
            Posicao v = p.deslocar(d[0], d[1]);
            if (isTransitavel(v)) {
                vizinhos.add(v);
            }
        }
        return vizinhos;
    }

    public synchronized void depositarFeromonio(Posicao p, TipoFeromonio tipo, float quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade de feromônio não pode ser negativa");
        }
        celula(p.x(), p.y());
        float[][] camada = feromonio[tipo.ordinal()];
        camada[p.x()][p.y()] = Math.min(TipoFeromonio.MAXIMO, camada[p.x()][p.y()] + quantidade);
    }

    public synchronized float feromonio(Posicao p, TipoFeromonio tipo) {
        celula(p.x(), p.y());
        return feromonio[tipo.ordinal()][p.x()][p.y()];
    }

    public synchronized float[][][] instantaneoFeromonio() {
        float[][][] copia = new float[feromonio.length][largura][];
        for (int t = 0; t < feromonio.length; t++) {
            for (int x = 0; x < largura; x++) {
                copia[t][x] = feromonio[t][x].clone();
            }
        }
        return copia;
    }

    public synchronized void atualizarFormiga(String nome, Posicao p, boolean carregandoAlimento) {
        celula(p.x(), p.y());
        formigas.put(nome, new PosicaoFormiga(p, carregandoAlimento));
    }

    public synchronized void removerFormiga(String nome) {
        formigas.remove(nome);
    }

    public synchronized List<PosicaoFormiga> getFormigas() {
        return List.copyOf(formigas.values());
    }

    public synchronized void entregarAlimento() {
        alimentoNoNinho++;
    }

    public synchronized int getAlimentoNoNinho() {
        return alimentoNoNinho;
    }

    public synchronized TipoCelula[][] instantaneo() {
        TipoCelula[][] tipos = new TipoCelula[largura][altura];
        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                tipos[x][y] = celulas[x][y].getTipo();
            }
        }
        return tipos;
    }

    private Celula celula(int x, int y) {
        if (!dentroDosLimites(x, y)) {
            throw new IndexOutOfBoundsException("Posição fora da grade: (" + x + ", " + y + ")");
        }
        return celulas[x][y];
    }

    private Celula celulaLivre(Posicao p) {
        Celula c = celula(p.x(), p.y());
        if (c.getTipo() != TipoCelula.VAZIA) {
            throw new IllegalStateException("Célula " + p + " já ocupada por " + c.getTipo());
        }
        return c;
    }
}
