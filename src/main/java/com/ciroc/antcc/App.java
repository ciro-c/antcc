package com.ciroc.antcc;

import com.ciroc.antcc.agentes.Formiga;
import com.ciroc.antcc.agentes.ParametrosFormiga;
import com.ciroc.antcc.ambiente.Ambiente;
import com.ciroc.antcc.ambiente.Cenarios;
import com.ciroc.antcc.plataforma.PlataformaJade;
import com.ciroc.antcc.render.Janela;
import com.ciroc.antcc.render.RenderizadorAmbiente;

public final class App {

    private static final int LARGURA_JANELA = 1024;
    private static final int ALTURA_JANELA = 768;
    private static final int FORMIGAS_PADRAO = 50;

    private App() {
    }

    public static void main(String[] args) {
        boolean guiJade = Boolean.getBoolean("antcc.jade.gui");
        int quantidadeFormigas = Integer.getInteger("antcc.formigas", FORMIGAS_PADRAO);

        Ambiente ambiente = Cenarios.c1();

        try (PlataformaJade plataforma = new PlataformaJade(guiJade);
             Janela janela = new Janela("antcc", LARGURA_JANELA, ALTURA_JANELA)) {
            plataforma.iniciarAgentes("formiga", quantidadeFormigas, Formiga.class,
                    ambiente, ParametrosFormiga.padrao());
            janela.abrir();
            janela.executar(new RenderizadorAmbiente(ambiente));
        }
        System.exit(0);
    }
}
