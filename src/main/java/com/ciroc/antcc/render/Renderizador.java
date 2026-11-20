package com.ciroc.antcc.render;

@FunctionalInterface
public interface Renderizador {

    /**
     * @param larguraFramebuffer largura atual do framebuffer em pixels
     * @param alturaFramebuffer  altura atual do framebuffer em pixels
     */
    void desenhar(int larguraFramebuffer, int alturaFramebuffer);
}
