package application;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import rendererEngine.Model;
import rendererEngine.ModelLoader;
import rendererEngine.Renderer;

public class FirstPersonCameraDemo {
    GLFWErrorCallback errorCallback;
    GLFWKeyCallback keyCallback;
    GLFWFramebufferSizeCallback fbCallback;
    GLFWCursorPosCallback cpCallback;

    long window;
    int width = 800;
    int height = 600;
    boolean windowed = true;
    float FOV = 45;
    float fViewDistance = 1000f;
    float cViewDistance = 0.01f;
    
    float mouseX, mouseY;
    boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST + 1];
    float heightAboveGround = 1f;
    float movementSpeed = 10f;
    int gridSize = 5;
    float ceiling = 100.0f;
    
    static ModelLoader loader = new ModelLoader();
	static Renderer renderer = new Renderer();
	

    void run() {
        try {
            init();
            loop();

            glfwDestroyWindow(window);
            keyCallback.free();
            fbCallback.free();
            cpCallback.free();
        } finally {
            glfwTerminate();
            errorCallback.free();
        }
    }

    void init() {
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        if (!windowed) {
            width = vidmode.width();
            height = vidmode.height();
        }
        window = glfwCreateWindow(width, height, "Cangen Java", !windowed ? monitor : NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        System.out.println("Press ESC to close the application.");
        System.out.println("Press W/S to move forward/backward.");
        System.out.println("Press A/D to strave left/right.");
        System.out.println("Press left shift/ctrl to move faster/slower.");
        System.out.println("Move the mouse to rotate.");
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    glfwSetWindowShouldClose(window, true);

                if (action == GLFW_PRESS || action == GLFW_REPEAT)
                    keyDown[key] = true;
                else
                    keyDown[key] = false;
            }
        });
        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });
        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
            public void invoke(long window, double xpos, double ypos) {
                mouseX = (float)xpos / width;
                mouseY = (float)ypos / height;
            }
        });

        if (windowed) {
            glfwSetWindowPos(window, (vidmode.width() - width) / 2, (vidmode.height() - height) / 2);
        }

        IntBuffer framebufferSize = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
        width = framebufferSize.get(0);
        height = framebufferSize.get(1);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
    }

    void loop() {
        GL.createCapabilities();
        glClearColor(0.97f, 0.97f, 0.97f, 1.0f);

        long lastTime = System.nanoTime();

        Vector3f dir = new Vector3f();
        Vector3f right = new Vector3f();
        Matrix4f mat = new Matrix4f();
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        Vector3f pos = new Vector3f(0, heightAboveGround, 0);
        float rotX = 0.0f;
        float rotY = 0.0f;
        

    	  float[] vertices = {
    			    -0.5f, 0.5f, 0f,
    			    -0.5f, -0.5f, 0f,
    			    0.5f, -0.5f, 0f,
    			    0.5f, -0.5f, 0f,
    			    0.5f, 0.5f, 0f,
    			    -0.5f, 0.5f, 0f
    			  };
        
        while (!glfwWindowShouldClose(window)) {
            long thisTime = System.nanoTime();
            float diff = (float) ((thisTime - lastTime) / 1E9);
            lastTime = thisTime;
            float move = diff * movementSpeed;

            if (keyDown[GLFW_KEY_LEFT_SHIFT])
                pos.y = (float) (pos.y - 0.01);
            if (keyDown[GLFW_KEY_SPACE])
            	pos.y = (float) (pos.y + 0.01);
            mat.positiveZ(dir).negate().mul(move);
            dir.y = 0.0f; // <- restrict movement on XZ plane
            mat.positiveX(right).mul(move);

            if (keyDown[GLFW_KEY_W])
                pos.add(dir);
            if (keyDown[GLFW_KEY_S])
                pos.sub(dir);
            if (keyDown[GLFW_KEY_A])
                pos.sub(right);
            if (keyDown[GLFW_KEY_D])
                pos.add(right);
            
            rotX = mouseY;
            rotY = mouseX;
            
            if(rotX >= 80f) {
            	mouseX = 80;
            }
            
            if(rotX <= -80f) {
            	mouseX = -80;
            }
            
            if(rotY >= 80f) {
            	mouseY = 80;
            }
            
            if(rotY <= -80f) {
            	mouseY = -80;
            }
            
            renderer.prepare();
            Model model = loader.loadToVAO(vertices);
            renderer.render(model);
            loader.cleanUp();

            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(mat.setPerspective((float) Math.toRadians(FOV), (float) width / height, cViewDistance, fViewDistance).get(fb));

            glMatrixMode(GL_MODELVIEW);
            mat.identity()
               .rotateX(rotX)
               .rotateY(rotY)
               .translate(-pos.x, -pos.y, -pos.z);
            glLoadMatrixf(mat.get(fb));
            
            glViewport(0, 0, width, height);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new FirstPersonCameraDemo().run();
        
    }
}