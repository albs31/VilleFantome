package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class WarningScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Texture warningTexture;
    private Texture blackTexture; 
    private Stage stage;
    private boolean inputSet = false;
    private boolean showControls = false;

    private float fadeAlpha = 0.0f; 
    private float fadeSpeed = 2.5f; 
    private boolean isExiting = false; 

    public WarningScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        warningTexture = new Texture("warningscreen.png");
        blackTexture = new Texture("pause_screen.png"); 
        stage = new Stage(new FitViewport(1280, 720));

        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isExiting = true; 
            }
        });
    }

    @Override
    public void render(float delta) {
        if (isExiting) {
            fadeAlpha += delta * fadeSpeed; 
            if (fadeAlpha >= 1.0f) {
                game.setScreen(new GameScreen(game)); 
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!inputSet) {
            Gdx.input.setInputProcessor(stage);
            inputSet = true;
        }

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.draw(warningTexture, 0, 0, 1280, 720);
        if (fadeAlpha > 0) {
            batch.setColor(0, 0, 0, fadeAlpha); 
            batch.draw(blackTexture, 0, 0, 1280, 720);
            batch.setColor(1, 1, 1, 1); 
        }
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        inputSet = false;
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (warningTexture != null) warningTexture.dispose();
        if (blackTexture != null) blackTexture.dispose();
        if (stage != null) stage.dispose();
    }
}