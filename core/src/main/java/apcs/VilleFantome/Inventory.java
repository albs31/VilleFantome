package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Inventory {
    private boolean isOpen = false;
    private ShapeRenderer shapeRenderer;
    private Stage stage;

    public Inventory() {
        shapeRenderer = new ShapeRenderer();
        stage = new Stage(new FitViewport(1280, 720));
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            isOpen = !isOpen;
        }
    }

    public void render(float delta) {
        if (!isOpen) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f); // 0.6f means 60% opacity
        shapeRenderer.rect(0, 0, 1280, 720);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        shapeRenderer.dispose();
        stage.dispose();
    }

    public boolean isOpen() {
        return isOpen;
    }
}
