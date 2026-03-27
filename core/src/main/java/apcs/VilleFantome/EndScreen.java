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

    private Texture endScreen1;  // End_Screen (1).png  — flashes briefly
    private Texture endScreen2;  // End_Screen.png      — holds for a moment
    private Texture credits;     // creditscene.png     — fades in last

    private float slideTimer = 0f;

    private static final float FLASH_DURATION = 0.4f; // was 0.12f, try 0.4–0.8f to taste // how long End_Screen (1) flashes (seconds)
    private static final float HOLD_DURATION  = 6.0f;  // how long End_Screen.png holds
    private static final float FADE_DURATION  = 1.8f;  // how long the crossfade to credits takes

    // 0 = flash,  1 = hold,  2 = crossfading to credits,  3 = credits fully visible
    private int currentSlide = 0;

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
        slideTimer   = 0f;
        keyHeld      = false;

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        slideTimer += delta;

        // Slide 0: flash — very brief, auto-advance
        if (currentSlide == 0 && slideTimer >= FLASH_DURATION) {
            currentSlide = 1;
            slideTimer = 0f;
        }

        // Slide 1: hold End_Screen.png, then start crossfade
        if (currentSlide == 1 && slideTimer >= HOLD_DURATION) {
            currentSlide = 2;
            slideTimer = 0f;
        }

        // Slide 2: crossfade End_Screen.png → credits
        if (currentSlide == 2 && slideTimer >= FADE_DURATION) {
            currentSlide = 3;
            slideTimer = 0f;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        if (currentSlide == 0) {
            // Flash: full opacity
            batch.setColor(1, 1, 1, 1);
            batch.draw(endScreen1, 0, 0, 1280, 720);

        } else if (currentSlide == 1) {
            // Hold: full opacity
            batch.setColor(1, 1, 1, 1);
            batch.draw(endScreen2, 0, 0, 1280, 720);

        } else if (currentSlide == 2) {
            // Crossfade: endScreen2 fades out, credits fade in simultaneously
            float progress = slideTimer / FADE_DURATION; // 0.0 → 1.0
            batch.setColor(1, 1, 1, 1f - progress);
            batch.draw(endScreen2, 0, 0, 1280, 720);
            batch.setColor(1, 1, 1, progress);
            batch.draw(credits, 0, 0, 1280, 720);

        } else {
            // Slide 3: credits fully visible, wait for key press
            batch.setColor(1, 1, 1, 1);
            batch.draw(credits, 0, 0, 1280, 720);
        }

        // Always reset batch color before ending
        batch.setColor(1, 1, 1, 1);
        batch.end();

        // Only listen for input on the credits slide
        if (currentSlide == 3) {
            boolean pressing = Gdx.input.isKeyPressed(Input.Keys.E)
                            || Gdx.input.isKeyPressed(Input.Keys.ENTER)
                            || Gdx.input.isKeyPressed(Input.Keys.SPACE);
            if (pressing && !keyHeld) {
                keyHeld = true;
                game.setScreen(new LoadingScreen(game));
            }
            if (!pressing) keyHeld = false;
        }
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