package com.ciroc.antcc.agentes;

public record ParametrosFormiga(long periodoMs,
                                float depositoFeromonio,
                                float decaimentoDeposito,
                                double influenciaFeromonio,
                                Long semente) {

    public static ParametrosFormiga padrao() {
        return new ParametrosFormiga(50, 10f, 0.99f, 2.0, null);
    }

    public ParametrosFormiga {
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Período deve ser positivo");
        }
        if (depositoFeromonio < 0) {
            throw new IllegalArgumentException("Depósito de feromônio não pode ser negativo");
        }
        if (decaimentoDeposito <= 0 || decaimentoDeposito > 1) {
            throw new IllegalArgumentException("Decaimento do depósito deve estar em (0, 1]");
        }
        if (influenciaFeromonio < 0) {
            throw new IllegalArgumentException("Influência do feromônio não pode ser negativa");
        }
    }

    public ParametrosFormiga comSemente(Long novaSemente) {
        return new ParametrosFormiga(periodoMs, depositoFeromonio, decaimentoDeposito, influenciaFeromonio, novaSemente);
    }
}
