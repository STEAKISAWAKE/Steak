package rendererEngine;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Renderer {
	public void prepare() {
		GL11.glClearColor(0f, 0.5f, 0.5f, 1f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	}
	
	public void render(Model model) {
		GL30.glBindVertexArray(model.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getVertexCount());
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
}
