package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class EndScreen implements Screen {

    private Main game;
    private SpriteBatch batch;
    private Stage stage;

    private float slideTimer = 0f;
    private static final float FLASH_DURATION = 0.15f;  // how fast End_Screen (1) flashes
    private static final float HOLD_DURATION  = 3.0f;   // how long End_Screen stays up

    private Texture endScreen1;   // End_Screen (1).png  — shown first
    private Texture endScreen2;   // End_Screen.png      — shown second
    private Texture credits;      // creditscene.png     — shown third / last

    private int currentSlide = 0; // 0, 1, 2
    private boolean keyHeld = false;

    public EndScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));

        endScreen1 = new Texture("End_Screen (1).png");
        endScreen2 = new Texture("End_Screen.png");
        credits    = new Texture("creditscene.png");

        currentSlide = 0;
        keyHeld = false;

        Gdx.input.setInputProcessor(null);
    }

    @Override
public void render(float delta) {
    slideTimer += delta;

    // Slide 0: flashes briefly, then auto-advances to slide 1
    if (currentSlide == 0 && slideTimer >= FLASH_DURATION) {
        currentSlide = 1;
        slideTimer = 0f;
    }

    // Slide 1: stays for HOLD_DURATION, then auto-advances to credits
    if (currentSlide == 1 && slideTimer >= HOLD_DURATION) {
        currentSlide = 2;
        slideTimer = 0f;
    }

    // Slide 2 (credits): waits for a key press to go to main menu
    if (currentSlide == 2) {
        boolean pressing = Gdx.input.isKeyPressed(Input.Keys.E)
                        || Gdx.input.isKeyPressed(Input.Keys.ENTER)
                        || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (pressing && !keyHeld) {
            keyHeld = true;
            game.setScreen(new LoadingScreen(game));
            return;
        }
        if (!pressing) keyHeld = false;
    }

    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.setProjectionMatrix(stage.getCamera().combined);
    batch.begin();

    if      (currentSlide == 0) batch.draw(endScreen1, 0, 0, 1280, 720);
    else if (currentSlide == 1) batch.draw(endScreen2, 0, 0, 1280, 720);
    else if (currentSlide == 2) batch.draw(credits,    0, 0, 1280, 720);

    batch.end();
}

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }
    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        endScreen1.dispose();
        endScreen2.dispose();
        credits.dispose();
    }
}