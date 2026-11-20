package com.ciroc.antcc.plataforma;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlataformaJade implements AutoCloseable {

    private final ContainerController container;
    private final List<AgentController> agentes = new ArrayList<>();


    public PlataformaJade(boolean comGui) {
        Runtime runtime = Runtime.instance();
        // A aplicação controla o encerramento da JVM (a janela é quem decide quando sair).
        runtime.setCloseVM(false);

        Profile perfil = new ProfileImpl(true);
        perfil.setParameter(Profile.GUI, Boolean.toString(comGui));
        container = runtime.createMainContainer(perfil);
        if (container == null) {
            throw new IllegalStateException("Não foi possível criar o contêiner principal do JADE");
        }
    }


    public AgentController iniciarAgente(String nome, Class<? extends Agent> classe, Object... argumentos) {
        try {
            AgentController agente = container.createNewAgent(nome, classe.getName(), argumentos);
            agente.start();
            agentes.add(agente);
            return agente;
        } catch (StaleProxyException e) {
            throw new IllegalStateException("Falha ao iniciar o agente " + nome, e);
        }
    }

    public List<AgentController> iniciarAgentes(String prefixo, int quantidade,
                                                Class<? extends Agent> classe, Object... argumentos) {
        List<AgentController> criados = new ArrayList<>(quantidade);
        for (int i = 0; i < quantidade; i++) {
            criados.add(iniciarAgente(prefixo + "-" + i, classe, argumentos));
        }
        return criados;
    }

    public List<AgentController> getAgentes() {
        return Collections.unmodifiableList(agentes);
    }

    public ContainerController getContainer() {
        return container;
    }

    @Override
    public void close() {
        try {
            container.kill();
        } catch (StaleProxyException e) {
            // O contêiner já foi encerrado.
        }
        Runtime.instance().shutDown();
    }
}
