package com.ciroc.antcc.ambiente;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Evaporacao implements AutoCloseable {

    private final ScheduledExecutorService executor;

    public Evaporacao(Ambiente ambiente, float taxa, long periodoMs) {
        if (taxa < 0 || taxa > 1) {
            throw new IllegalArgumentException("Taxa de evaporação deve estar em [0, 1]");
        }
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Período deve ser positivo");
        }
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "evaporacao");
            t.setDaemon(true);
            return t;
        });
        if (taxa > 0) {
            executor.scheduleAtFixedRate(() -> ambiente.evaporar(taxa), periodoMs, periodoMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
