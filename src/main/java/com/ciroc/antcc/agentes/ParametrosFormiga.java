package com.ciroc.antcc.agentes;

public record ParametrosFormiga(long periodoMs, float depositoFeromonio, Long semente) {

    public static ParametrosFormiga padrao() {
        return new ParametrosFormiga(50, 5f, null);
    }

    public ParametrosFormiga {
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Período deve ser positivo");
        }
        if (depositoFeromonio < 0) {
            throw new IllegalArgumentException("Depósito de feromônio não pode ser negativo");
        }
    }
}
