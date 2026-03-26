package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
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

public class PawnShopScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private Player player;
    private Inventory inventory;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private Texture pawnShop1;
    private Texture pawnShop2;

    private Texture pauseBg;
    private Texture resumeTex;
    private Texture quitTex;
    private Texture enterSign;

    private ImageButton resumeButton;
    private ImageButton quitButton;

    private Sound pickupSound;

    private int currentRoom = 1;

    private Rectangle playerBounds;
    private Rectangle exitBounds;

    private boolean showEnterSign = false;
    private boolean canExitShop = false;

    private float movementDelayTimer = 0f;

    // make this smaller for faster transition timing
    // old was 0.20f
    private final float MAX_DELAY = 0.04f;

    // change this number if you want a bigger/smaller collision feel in pawn shop
    private float playerScale = 1.0f;

    public PawnShopScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        inventory = new Inventory();

        Gdx.input.setInputProcessor(null);

        pawnShop1 = new Texture("PawnShop1.png");
        pawnShop2 = new Texture("PawnShop2.png");

        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        enterSign = new Texture("exitsign.png");

        pickupSound = null;

        player = new Player(350, -100);
        player.setDrawSize(1000, 1000);
        playerBounds = new Rectangle();

        // left edge of room 2 = exit back outside
        // change width/height if needed
        exitBounds = new Rectangle(1080, 10, 200, 350);

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
                game.setScreen(new GameScreen(game, true));
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            if (state == State.PAUSED) {
                Gdx.input.setInputProcessor(stage);
            } else {
                Gdx.input.setInputProcessor(null);
            }
        }

        if (state == State.RUNNING) {
            updateGame(delta);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        if (currentRoom == 1) {
            batch.draw(pawnShop1, 0, 0, 1280, 720);
        } else {
            batch.draw(pawnShop2, 0, 0, 1280, 720);
        }

        player.draw(batch);

        if (showEnterSign) {
            if (currentRoom == 2) {
                // move these numbers if you want the sign in a different spot
                batch.draw(enterSign, 700, 450, 800, 800);
            }
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
        // tiny delay only, so transitions feel quicker
        if (movementDelayTimer < MAX_DELAY) {
            movementDelayTimer += delta;
            return;
        }

        player.update(delta);

        showEnterSign = false;
        canExitShop = false;

        // hitbox tuned similar to your GameScreen setup
        playerBounds.set(
            player.x + (170 * playerScale),
            player.y + (40 * playerScale),
            140 * playerScale,
            260 * playerScale
        );

        if (currentRoom == 1) {
            
            if (player.x < -20) {
                player.x = -20;
            }

           
            if (player.x >= 1080) {
                currentRoom = 2;

                // spawn farther into room 2 so it feels instant
                player.x = 80;

                movementDelayTimer = 0f;
                return;
            }
        } else if (currentRoom == 2) {
            // allow going back to room 1 from the far left
            if (player.x <= -80) {
                currentRoom = 1;

                // spawn farther into room 1
                player.x = 1040;

                movementDelayTimer = 0f;
                return;
            }

            
            if (player.x > 1120) {
                player.x = 1120;
            }

            
            if (player.x <= 70 || playerBounds.overlaps(exitBounds)) {
                showEnterSign = true;
                canExitShop = true;
            }

            if (canExitShop && Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                game.setScreen(new GameScreen(game, true));
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        inventory.dispose();
        player.dispose();

        pawnShop1.dispose();
        pawnShop2.dispose();

        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        enterSign.dispose();

        if (pickupSound != null) pickupSound.dispose();
    }
}
