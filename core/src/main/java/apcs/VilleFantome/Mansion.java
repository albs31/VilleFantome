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

    private enum State { RUNNING, PAUSED, EVIDENCE }
    private State state = State.RUNNING;

    private Texture mansion3, mansion1, mansion2;
    private Texture pauseBg, resumeTex, quitTex;
    private Texture pickupPrompt, evidenceTex1, evidenceTex2;
    private ImageButton resumeButton, quitButton;

    private Rectangle playerBounds, itemHitbox1, itemHitbox2;
    private boolean showPickupPrompt = false;
    private boolean item1PickedUp = false;
    private boolean item2PickedUp = false;
    private int evidenceToShow = 0;

    private float movementDelayTimer = 0f;
    private final float MAX_DELAY = 0.04f;

    private float returnX, returnY;

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
        pickupPrompt = new Texture("pickupitem.png");
        evidenceTex1 = new Texture("Diary Entry 3.png");
        evidenceTex2 = new Texture("FinalDiaryEntry.png");

        player = new Player(20, -100);
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f);

        playerBounds = new Rectangle();

        // Adjust x values to match where items appear in each background
        itemHitbox1 = new Rectangle(400, 0, 150, 720); // Diary Entry 3 in Mansion 3.png
        itemHitbox2 = new Rectangle(400, 0, 150, 720); // Final Diary Entry in Mansion 1.png

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
                SaveManager.save(returnX, returnY, 2, 7, "mansion");
                game.setScreen(new LoadingScreen(game));
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);
    }

    @Override
    public void render(float delta) {
        if (state == State.EVIDENCE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                state = State.RUNNING;
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
                Gdx.input.setInputProcessor(state == State.PAUSED ? stage : null);
            }
            if (state == State.RUNNING) updateGame(delta);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        if (currentRoom == 1) batch.draw(mansion3, 0, 0, 1280, 720);
        else if (currentRoom == 2) batch.draw(mansion1, 0, 0, 1280, 720);
        else batch.draw(mansion2, 0, 0, 1280, 720);

        player.draw(batch);

        if (showPickupPrompt) batch.draw(pickupPrompt, 0, 0, 1280, 720);
        if (state == State.PAUSED) batch.draw(pauseBg, 0, 0, 1280, 720);
        if (state == State.EVIDENCE) {
            if (evidenceToShow == 1) batch.draw(evidenceTex1, 0, 0, 1280, 720);
            if (evidenceToShow == 2) batch.draw(evidenceTex2, 0, 0, 1280, 720);
        }

        batch.end();

        inventory.handleInput();
        inventory.render(delta);

        if (state == State.PAUSED) { stage.act(delta); stage.draw(); }
    }

    private void updateGame(float delta) {
        if (movementDelayTimer < MAX_DELAY) {
            movementDelayTimer += delta;
            return;
        }

        player.update(delta);
        playerBounds.set(player.x + 170, player.y + 40, 140, 260);

        if (player.x < -300) player.x = -300;

        showPickupPrompt = false;

        if (currentRoom == 1) {
            if (!item1PickedUp && playerBounds.overlaps(itemHitbox1)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("Diary Entry 3", "Diary Entry 3.png");
                    item1PickedUp = true;
                    evidenceToShow = 1;
                    state = State.EVIDENCE;
                }
            }
            if (player.x > 900) {
                currentRoom = 2;
                player.x = -250;
            }
        } else if (currentRoom == 2) {
            if (!item2PickedUp && playerBounds.overlaps(itemHitbox2)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("Final Diary Entry", "FinalDiaryEntry.png");
                    item2PickedUp = true;
                    evidenceToShow = 2;
                    state = State.EVIDENCE;
                }
            }
            if (player.x > 900) {
                currentRoom = 3;
                player.x = -250;
            }
        } else if (currentRoom == 3) {
            if (player.x > 900) player.x = 900;
        }
    }

    @Override
    public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
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
        pickupPrompt.dispose();
        evidenceTex1.dispose();
        evidenceTex2.dispose();
    }
}
