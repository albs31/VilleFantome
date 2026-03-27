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

public class Mansion implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private Player player;
    private Inventory inventory;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private Texture mansion3, mansion1, mansion2;
    private Texture pauseBg, resumeTex, quitTex, exitSign;
    private ImageButton resumeButton, quitButton;

    private Rectangle playerBounds, exitHitbox;
    private boolean showExitSign = false;

    private float movementDelayTimer = 0f;
    private final float MAX_DELAY = 0.04f;

    private float returnX, returnY;

    // 1 = Mansion 3, 2 = Mansion 1, 3 = Mansion 2
    private int currentRoom = 1;

    public Mansion(Main game, float x, float y) {
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

        mansion3 = new Texture("Mansion 3.png");
        mansion1 = new Texture("Mansion 1.png");
        mansion2 = new Texture("Mansion 2.png");

        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        exitSign = new Texture("exitsign.png");

        // Spawn at left side of first mansion room
        player = new Player(20, -100);

        // Resize / speed inside mansion
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f);

        playerBounds = new Rectangle();

        // Right-side exit trigger area
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
        SaveManager.save(player.x, player.y, 2, 7, "mansion");
game.setScreen(new LoadingScreen(game));
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

        if (currentRoom == 1) {
            batch.draw(mansion3, 0, 0, 1280, 720);
        } else if (currentRoom == 2) {
            batch.draw(mansion1, 0, 0, 1280, 720);
        } else {
            batch.draw(mansion2, 0, 0, 1280, 720);
        }

        player.draw(batch);

        if (showExitSign && currentRoom == 3) {
            batch.draw(exitSign, 0, 0, 1280, 720);
        }

        if (state == State.PAUSED) {
            batch.draw(pauseBg, 0, 0, 1280, 720);
        }

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

        // Adjust if Theo's collision feels off
        playerBounds.set(player.x + 170, player.y + 40, 140, 260);

        showExitSign = false;

        // Prevent him from going too far left
        if (player.x < -300) {
            player.x = -300;
        }

        // Move between mansion rooms
        if (player.x > 900) {
            if (currentRoom == 1) {
                currentRoom = 2;
                player.x = -250;
                return;
            } else if (currentRoom == 2) {
                currentRoom = 3;
                player.x = -250;
                return;
            }
        }

        // Show exit sign only in final mansion room
        if (currentRoom == 3 && playerBounds.overlaps(exitHitbox)) {
            showExitSign = true;
        }

        // Exit final room back to town screen 2
        if (currentRoom == 3 && player.x > 900) {
            game.setScreen(new GameScreen(game, true, returnX, returnY, 2));
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

        mansion3.dispose();
        mansion1.dispose();
        mansion2.dispose();

        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        exitSign.dispose();
    }
}
