package com.ciroc.antcc.render;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Janela implements AutoCloseable {

    private final String titulo;
    private final int largura;
    private final int altura;
    private long handle = NULL;

    public Janela(String titulo, int largura, int altura) {
        this.titulo = titulo;
        this.largura = largura;
        this.altura = altura;
    }

    public void abrir() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Não foi possível inicializar o GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        handle = glfwCreateWindow(largura, altura, titulo, NULL, NULL);
        if (handle == NULL) {
            glfwTerminate();
            throw new IllegalStateException("Não foi possível criar a janela GLFW");
        }

        glfwSetKeyCallback(handle, (janela, tecla, scancode, acao, mods) -> {
            if (tecla == GLFW_KEY_ESCAPE && acao == GLFW_RELEASE) {
                glfwSetWindowShouldClose(janela, true);
            }
        });

        centralizar();

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1);
        GL.createCapabilities();

        glfwShowWindow(handle);
    }

    public void executar(Renderizador renderizador) {
        if (handle == NULL) {
            abrir();
        }
        glClearColor(0.08f, 0.08f, 0.08f, 1f);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            while (!glfwWindowShouldClose(handle)) {
                glfwGetFramebufferSize(handle, w, h);
                glViewport(0, 0, w.get(0), h.get(0));
                glClear(GL_COLOR_BUFFER_BIT);

                renderizador.desenhar(w.get(0), h.get(0));

                glfwSwapBuffers(handle);
                glfwPollEvents();
            }
        }
    }

    public void setTitulo(String titulo) {
        if (handle != NULL) {
            glfwSetWindowTitle(handle, titulo);
        }
    }

    @Override
    public void close() {
        if (handle != NULL) {
            glfwFreeCallbacks(handle);
            glfwDestroyWindow(handle);
            handle = NULL;
        }
        glfwTerminate();
        GLFWErrorCallback anterior = glfwSetErrorCallback(null);
        if (anterior != null) {
            anterior.free();
        }
    }

    private void centralizar() {
        // No Wayland a posição da janela é decidida pelo compositor.
        if (glfwGetPlatform() == GLFW_PLATFORM_WAYLAND) {
            return;
        }
        long monitor = glfwGetPrimaryMonitor();
        if (monitor == NULL) {
            return;
        }
        var modo = glfwGetVideoMode(monitor);
        if (modo != null) {
            glfwSetWindowPos(handle, (modo.width() - largura) / 2, (modo.height() - altura) / 2);
        }
    }
}
