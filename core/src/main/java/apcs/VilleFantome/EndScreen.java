package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class EndScreen implements Screen {

    private Main game;
    private SpriteBatch batch;
    private Stage stage;

    private Texture endScreen1, endScreen2, credits;
    private Texture pauseBg, resumeTex, quitTex; // Pause assets
    private ImageButton resumeButton, quitButton;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private float slideTimer = 0f;
    private static final float FLASH_DURATION = 0.6f;
    private static final float HOLD_DURATION  = 6.0f;
    private static final float FADE_DURATION  = 1.8f;

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
        
        // Load Pause Textures
        pauseBg    = new Texture("pause_screen.png");
        resumeTex  = new Texture("resume_button.png");
        quitTex    = new Texture("quitbutton.png");

        setupPauseMenu();

        currentSlide = 0;
        slideTimer   = 0f;
        keyHeld      = false;
        state        = State.RUNNING;
        
        // Ensure input processor is null initially unless paused
        Gdx.input.setInputProcessor(null);
    }

    private void setupPauseMenu() {
        resumeButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(resumeTex)));
        resumeButton.setPosition(500, 300);
        resumeButton.setSize(300, 120);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                state = State.RUNNING;
                Gdx.input.setInputProcessor(null);
            }
        });

        quitButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(quitTex)));
        quitButton.setPosition(500, 200);
        quitButton.setSize(300, 120);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoadingScreen(game));
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);
    }

    @Override
    public void render(float delta) {
        // Toggle Pause Logic
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            Gdx.input.setInputProcessor(state == State.PAUSED ? stage : null);
        }

        // Only update timers if running
        if (state == State.RUNNING) {
            slideTimer += delta;

            if (currentSlide == 0 && slideTimer >= FLASH_DURATION) {
                currentSlide = 1;
                slideTimer = 0f;
            }
            if (currentSlide == 1 && slideTimer >= HOLD_DURATION) {
                currentSlide = 2;
                slideTimer = 0f;
            }
            if (currentSlide == 2 && slideTimer >= FADE_DURATION) {
                currentSlide = 3;
                slideTimer = 0f;
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        // Rendering Slides
        if (currentSlide == 0) {
            batch.setColor(1, 1, 1, 1);
            batch.draw(endScreen1, 0, 0, 1280, 720);
        } else if (currentSlide == 1) {
            batch.setColor(1, 1, 1, 1);
            batch.draw(endScreen2, 0, 0, 1280, 720);
        } else if (currentSlide == 2) {
            float progress = slideTimer / FADE_DURATION;
            batch.setColor(1, 1, 1, 1f - progress);
            batch.draw(endScreen2, 0, 0, 1280, 720);
            batch.setColor(1, 1, 1, progress);
            batch.draw(credits, 0, 0, 1280, 720);
        } else {
            batch.setColor(1, 1, 1, 1);
            batch.draw(credits, 0, 0, 1280, 720);
        }

        // Draw Pause Background if paused
        if (state == State.PAUSED) {
            batch.setColor(1, 1, 1, 1);
            batch.draw(pauseBg, 0, 0, 1280, 720);
        }

        batch.setColor(1, 1, 1, 1);
        batch.end();

        // Credit Screen Input
        if (state == State.RUNNING && currentSlide == 3) {
            boolean pressing = Gdx.input.isKeyPressed(Input.Keys.E)
                            || Gdx.input.isKeyPressed(Input.Keys.ENTER)
                            || Gdx.input.isKeyPressed(Input.Keys.SPACE);
            if (pressing && !keyHeld) {
                keyHeld = true;
                game.setScreen(new LoadingScreen(game));
            }
            if (!pressing) keyHeld = false;
        }

        // Draw Buttons
        if (state == State.PAUSED) {
            stage.act(delta);
            stage.draw();
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
        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
    }
}