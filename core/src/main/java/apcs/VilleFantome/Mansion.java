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

    private enum State { RUNNING, PAUSED, EVIDENCE, DIALOGUE, ENDING }
    private State state = State.RUNNING;

    private Texture mansion3, mansion1, mansion2;
    private Texture pauseBg, resumeTex, quitTex;
    private Texture pickupPrompt, evidenceTex1, evidenceTex2;
    private ImageButton resumeButton, quitButton;

    // Dialogues
    private DialogueManager introDialogue;
    private DialogueManager postEvidence1Dialogue;
    private DialogueManager postEvidence2Dialogue;
    private DialogueManager room3Dialogue1;
    private DialogueManager room3Dialogue2;
    private DialogueManager activeDialogue = null;

    private Rectangle playerBounds, itemHitbox1, itemHitbox2;
    private boolean showPickupPrompt = false;
    private boolean item1PickedUp = false;
    private boolean item2PickedUp = false;
    private boolean introDialoguePlayed = false;
    private boolean room3DialoguePlayed = false;
    private int evidenceToShow = 0;

    private float movementDelayTimer = 0f;
    private final float MAX_DELAY = 0.04f;

    private float evidenceCooldown = 0f;

    private float returnX, returnY;

    private int currentRoom = 1;

    // Ending sequence
    private static final float WALK_TARGET_X = 300f; // tweak to centre of room 3

    public Mansion(Main game, float x, float y) {
        this.game = game;
        this.returnX = x;
        this.returnY = y;
    }

    private void startDialogue(DialogueManager dialogue) {
        activeDialogue = dialogue;
        activeDialogue.start();
        state = State.DIALOGUE;
    }

    @Override
    public void show() {
        batch     = new SpriteBatch();
        stage     = new Stage(new FitViewport(1280, 720));
        inventory = new Inventory();
        Gdx.input.setInputProcessor(null);

        mansion3     = new Texture("Mansion 3.png");
        mansion1     = new Texture("Mansion 1.png");
        mansion2     = new Texture("Mansion 2.png");
        pauseBg      = new Texture("pause_screen.png");
        resumeTex    = new Texture("resume_button.png");
        quitTex      = new Texture("quitbutton.png");
        pickupPrompt = new Texture("pickupitem.png");
        evidenceTex1 = new Texture("Diary Entry 3.png");
        evidenceTex2 = new Texture("FinalDiaryEntry.png");

        player = new Player(-300, -100);
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f);

        playerBounds = new Rectangle();
        itemHitbox1  = new Rectangle(400, 0, 150, 720);
        itemHitbox2  = new Rectangle(400, 0, 150, 720);

        // Dialogue 1 — plays immediately on entering Room 1
        introDialogue = new DialogueManager(
            new String[] { "MansionDialogue1.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // Dialogue 2 — plays after picking up Evidence 1
        postEvidence1Dialogue = new DialogueManager(
            new String[] { "MansionDialogue4.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // Dialogue 3 — plays after picking up Evidence 2
        postEvidence2Dialogue = new DialogueManager(
            new String[] { "FinalDialogue1.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // Dialogue 5 — second in Room 3 chain (declared first so room3Dialogue1 can reference it)
        // When finished, player walks to middle then EndScreen launches
        room3Dialogue2 = new DialogueManager(
            new String[] { "FinalDialogue3.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.ENDING;
            }
        );

        // Dialogue 4 — first in Room 3 chain, chains into room3Dialogue2
        room3Dialogue1 = new DialogueManager(
            new String[] { "FinalDialogue2.png" },
            new float[]  { 0f },
            () -> startDialogue(room3Dialogue2)
        );

        setupPauseMenu();

        // Trigger the intro dialogue immediately
        introDialoguePlayed = true;
        startDialogue(introDialogue);
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
        if (activeDialogue != null) activeDialogue.update(delta);

        if (state == State.EVIDENCE) {
            evidenceCooldown += delta;
            if (evidenceCooldown > 0.2f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                evidenceCooldown = 0f;
                if      (evidenceToShow == 1) startDialogue(postEvidence1Dialogue);
                else if (evidenceToShow == 2) startDialogue(postEvidence2Dialogue);
            }
        } else if (state == State.DIALOGUE) {
            // handled internally by DialogueManager
        } else if (state == State.ENDING) {
            updateGame(delta);
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

        if      (currentRoom == 1) batch.draw(mansion3, 0, 0, 1280, 720);
        else if (currentRoom == 2) batch.draw(mansion1, 0, 0, 1280, 720);
        else                       batch.draw(mansion2, 0, 0, 1280, 720);

        player.draw(batch);

        if (showPickupPrompt) batch.draw(pickupPrompt, 0, 0, 1280, 720);

        if (state == State.EVIDENCE) {
            if      (evidenceToShow == 1) batch.draw(evidenceTex1, 0, 0, 1280, 720);
            else if (evidenceToShow == 2) batch.draw(evidenceTex2, 0, 0, 1280, 720);
        }

        if (activeDialogue != null) activeDialogue.render(batch);

        if (state == State.PAUSED) batch.draw(pauseBg, 0, 0, 1280, 720);

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
        showPickupPrompt = false;

        // ENDING: let player walk right until they reach the target, then launch EndScreen
        if (state == State.ENDING) {
            if (player.x >= WALK_TARGET_X) {
                player.x = WALK_TARGET_X;
                game.setScreen(new EndScreen(game));
            }
            return;
        }

        if (currentRoom == 1) {
            if (player.x < -435) {
                game.setScreen(new GameScreen(game, true, returnX, returnY, 2));
                return;
            }

            if (!item1PickedUp && playerBounds.overlaps(itemHitbox1)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("Diary Entry 3", "Diary Entry 3.png");
                    item1PickedUp    = true;
                    evidenceToShow   = 1;
                    evidenceCooldown = 0f;
                    showPickupPrompt = false;
                    state = State.EVIDENCE;
                }
            }

            if (player.x > 900) {
                currentRoom = 2;
                player.x = -250;
            }

        } else if (currentRoom == 2) {
            if (player.x < -400) {
                currentRoom = 1;
                player.x = 850;
            }

            if (!item2PickedUp && playerBounds.overlaps(itemHitbox2)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("Final Diary Entry", "FinalDiaryEntry.png");
                    item2PickedUp    = true;
                    evidenceToShow   = 2;
                    evidenceCooldown = 0f;
                    showPickupPrompt = false;
                    state = State.EVIDENCE;
                }
            }

            if (player.x > 900) {
                currentRoom = 3;
                player.x = -250;
                if (!room3DialoguePlayed) {
                    room3DialoguePlayed = true;
                    startDialogue(room3Dialogue1);
                }
            }

        } else if (currentRoom == 3) {
            if (player.x < -400) {
                currentRoom = 2;
                player.x = 850;
            }
            if (player.x > 900) player.x = 900;
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
        introDialogue.dispose();
        postEvidence1Dialogue.dispose();
        postEvidence2Dialogue.dispose();
        room3Dialogue1.dispose();
        room3Dialogue2.dispose();
    }
}