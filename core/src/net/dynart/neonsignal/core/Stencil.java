package net.dynart.neonsignal.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Stencil {

    private final Mesh mesh;
    private final ShaderProgram shader;

    private FrameBuffer frameBuffer;
    private Batch batch;

    public Stencil() {
        mesh = new Mesh(true, 4, 6,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
        );
        shader = new ShaderProgram(
            Gdx.files.internal("data/shaders/stencil_vertex.glsl"),
            Gdx.files.internal("data/shaders/stencil_fragment.glsl")
        );
        if (!shader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        }
    }

    public void beginStencil(Camera camera, Batch batch) {
        this.batch = batch;
        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
    }

    public void endStencil() {
        batch.end();
        frameBuffer.end();
    }

     public void resize(int width, int height) {
        if (width != 0 && height != 0) {
            frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, width, height, false);
        }
    }

    public void draw(FrameBuffer screenFrameBuffer, Stage stage) {
        shader.bind();
        shader.setUniformMatrix("u_projTrans", stage.getCamera().combined);
        Texture frameTexture = screenFrameBuffer.getColorBufferTexture();
        frameTexture.bind(1);
        shader.setUniformi("u_texture", 1);
        Texture stencilFrameTexture = frameBuffer.getColorBufferTexture();
        stencilFrameTexture.bind(0);
        shader.setUniformi("u_texture2", 0);
        Viewport viewport = stage.getViewport();
        float x = 0;
        float y = 0;
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        float[] vertices = {
                /* Position: */ x, y, 0, /* UV: */ 0, 0,
                /* Position: */ w, y, 0, /* UV: */ 1, 0,
                /* Position: */ x, h, 0, /* UV: */ 0, 1,
                /* Position: */ w, h, 0, /* UV: */ 1, 1
        };
        short[] indices = { 0, 1, 2, 1, 3, 2 };
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        mesh.render(shader, GL30.GL_TRIANGLES);
    }

    public void dispose() {
        frameBuffer.dispose();
    }

}
