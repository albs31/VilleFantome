package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class JHouseScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private Player player;
    private Inventory inventory;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private Texture jRoom, pauseBg, resumeTex, quitTex, exitSign;
    private ImageButton resumeButton, quitButton;

    private Rectangle playerBounds, exitHitbox;
    private boolean showExitSign = false;

    private float movementDelayTimer = 0f;
    private final float MAX_DELAY = 0.04f;

    private float returnX, returnY;

    public JHouseScreen(Main game, float x, float y) {
        this.game = game;
        this.returnX = x;
        this.returnY = y;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        inventory = new Inventory();
        Gdx.input.setInputProcessor(null);

        jRoom = new Texture("j'sroom.png");
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        exitSign = new Texture("exitsign.png");

        // Spawn on the left side of J's room
        player = new Player(20, -100);

        // Resize / speed for inside room
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f);

        playerBounds = new Rectangle();

        // Right side exit trigger
        exitHitbox = new Rectangle(1000, 0, 280, 720);

        setupPauseMenu();
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
                game.setScreen(new GameScreen(game, true, returnX, returnY));
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            Gdx.input.setInputProcessor(state == State.PAUSED ? stage : null);
        }

        if (state == State.RUNNING) updateGame(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        batch.draw(jRoom, 0, 0, 1280, 720);
        player.draw(batch);

        if (showExitSign) {
            batch.draw(exitSign, 0, 0, 1280, 720);
        }

        if (state == State.PAUSED) batch.draw(pauseBg, 0, 0, 1280, 720);
        batch.end();

        inventory.handleInput();
        inventory.render(delta);

        if (state == State.PAUSED) {
            stage.act(delta);
            stage.draw();
        }
    }

    private void updateGame(float delta) {
        if (movementDelayTimer < MAX_DELAY) {
            movementDelayTimer += delta;
            return;
        }

        player.update(delta);

        // Adjust this hitbox if Theo inside this room feels off
        playerBounds.set(player.x + 170, player.y + 40, 140, 260);

        showExitSign = false;

        // Stop him from going too far left
        if (player.x < -300) player.x = -300;

        // Show exit sign near right side
        if (playerBounds.overlaps(exitHitbox)) {
            showExitSign = true;
        }

        // Leave room and return to town
        if (player.x > 900) {
            game.setScreen(new GameScreen(game, true, returnX, returnY));
        }
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        inventory.dispose();
        player.dispose();
        jRoom.dispose();
        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        exitSign.dispose();
    }
}
