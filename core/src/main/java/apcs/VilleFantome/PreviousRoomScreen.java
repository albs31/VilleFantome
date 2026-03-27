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

public class PreviousRoomScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Texture roomPt1, roomPt2, exitSign;
    private Player player;
    private Stage stage;
    private Inventory inventory;

    private enum State { WAITING, RUNNING, PAUSED, EVIDENCE, DIALOGUE }
    private State state = State.WAITING;

    private Texture pauseBg, resumeTex, quitTex;
    private ImageButton resumeButton, quitButton;
    private Texture pickupPrompt, evidenceTex1, evidenceTex2, evidenceTex3, evidenceTex4;

    private DialogueManager entryDialogue;
    private DialogueManager postEvidence1Dialogue;
    private DialogueManager postEvidence2Dialogue;
    private DialogueManager postEvidence3Dialogue;
    private DialogueManager postEvidence4Dialogue;
    private DialogueManager activeDialogue = null;

    private int currentRoom = 1;
    private Rectangle playerBounds, exitHitbox, itemHitbox1, itemHitbox2, itemHitbox3, itemHitbox4;
    private boolean showExitSign = false;
    private boolean showPickupPrompt = false;
    private boolean item1PickedUp = false;
    private boolean item2PickedUp = false;
    private boolean item3PickedUp = false;
    private boolean item4PickedUp = false;
    private int evidenceToShow = 0;

    private float movementDelayTimer = 0f;
    private final float MAX_DELAY = 0.04f;

    private float entryDialogueTimer = 0f;
    private final float ENTRY_DIALOGUE_DELAY = 2.0f;

    private float evidenceCooldown = 0f;

    private float returnX, returnY;

    public PreviousRoomScreen(Main game, float x, float y) {
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
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        inventory = new Inventory();
        Gdx.input.setInputProcessor(null);

        roomPt1 = new Texture("His_Previous_Room1.png");
        roomPt2 = new Texture("His_Previous_Room2.png");
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        exitSign = new Texture("exitsign.png");
        pickupPrompt = new Texture("pickupitem.png");
        evidenceTex1 = new Texture("J's Letter 1.png");
        evidenceTex2 = new Texture("J's Letter 2.png");
        evidenceTex3 = new Texture("scrapped letter 1.png");
        evidenceTex4 = new Texture("Scrapped letter 2.png");

        player = new Player(-200, -100);
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f);

        playerBounds = new Rectangle();
        exitHitbox = new Rectangle(1000, 0, 200, 720);
        itemHitbox1 = new Rectangle(150, 0, 200, 720);
        itemHitbox2 = new Rectangle(500, 0, 200, 720);
        itemHitbox3 = new Rectangle(400, 0, 200, 720);
        itemHitbox4 = new Rectangle(865, 0, 200, 720);

        // SEQUENCE 1 — entry dialogue
        entryDialogue = new DialogueManager(
            new String[] { "PreviousRoomDialogue1.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // SEQUENCE 2 — after evidence 1
        postEvidence1Dialogue = new DialogueManager(
            new String[] { "PreviousRoomDialogue2.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // SEQUENCE 3 — after evidence 2
        postEvidence2Dialogue = new DialogueManager(
            new String[] { "PreviousRoomDialogue3.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // SEQUENCE 4 — after evidence 3
        postEvidence3Dialogue = new DialogueManager(
            new String[] { "PreviousRoomDialogue4.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // SEQUENCE 5 — after evidence 4
        postEvidence4Dialogue = new DialogueManager(
            new String[] { "PreviousRoomDialogue5.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        state = State.WAITING;

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
                game.setScreen(new GameScreen(game, true, returnX, returnY, 1));
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);
    }

    @Override
    public void render(float delta) {
        if (activeDialogue != null) activeDialogue.update(delta);

        if (state == State.WAITING) {
            entryDialogueTimer += delta;
            player.update(delta);
            if (entryDialogueTimer >= ENTRY_DIALOGUE_DELAY) {
                startDialogue(entryDialogue);
            }
        } else if (state == State.EVIDENCE) {
            evidenceCooldown += delta;
            if (evidenceCooldown > 0.2f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                evidenceCooldown = 0f;
                if (evidenceToShow == 1) startDialogue(postEvidence1Dialogue);
                else if (evidenceToShow == 2) startDialogue(postEvidence2Dialogue);
                else if (evidenceToShow == 3) startDialogue(postEvidence3Dialogue);
                else if (evidenceToShow == 4) startDialogue(postEvidence4Dialogue);
            }
        } else if (state == State.DIALOGUE) {
            // handled internally by DialogueManager
        } else {
            if (state != State.EVIDENCE && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
                Gdx.input.setInputProcessor(state == State.PAUSED ? stage : null);
            }
            if (state == State.RUNNING) updateGame(delta);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        batch.draw(currentRoom == 1 ? roomPt1 : roomPt2, 0, 0, 1280, 720);
        player.draw(batch);

        if (currentRoom == 2 && showExitSign) batch.draw(exitSign, 0, 0, 1280, 720);
        if (showPickupPrompt) batch.draw(pickupPrompt, 0, 0, 1280, 720);

        if (state == State.EVIDENCE) {
            if (evidenceToShow == 1) batch.draw(evidenceTex1, 0, 0, 1280, 720);
            else if (evidenceToShow == 2) batch.draw(evidenceTex2, 0, 0, 1280, 720);
            else if (evidenceToShow == 3) batch.draw(evidenceTex3, 0, 0, 1280, 720);
            else if (evidenceToShow == 4) batch.draw(evidenceTex4, 0, 0, 1280, 720);
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

        showExitSign = false;
        showPickupPrompt = false;

        if (currentRoom == 1) {
            if (!item1PickedUp && playerBounds.overlaps(itemHitbox1)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("J's Letter 1", "J's Letter 1.png");
                    item1PickedUp = true;
                    evidenceToShow = 1;
                    evidenceCooldown = 0f;
                    showPickupPrompt = false;
                    state = State.EVIDENCE;
                }
            }
            if (!item2PickedUp && playerBounds.overlaps(itemHitbox2)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("J's Letter 2", "J's Letter 2.png");
                    item2PickedUp = true;
                    evidenceToShow = 2;
                    evidenceCooldown = 0f;
                    showPickupPrompt = false;
                    state = State.EVIDENCE;
                }
            }

            if (player.x < -300) player.x = -300;
            if (player.x >= 1100) {
                currentRoom = 2;
                player.x = -270;
            }

        } else if (currentRoom == 2) {
            if (playerBounds.overlaps(exitHitbox)) showExitSign = true;

            if (!item3PickedUp && playerBounds.overlaps(itemHitbox3)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("Scrapped Letter 1", "scrapped letter 1.png");
                    item3PickedUp = true;
                    evidenceToShow = 3;
                    evidenceCooldown = 0f;
                    showPickupPrompt = false;
                    state = State.EVIDENCE;
                }
            }
            if (!item4PickedUp && playerBounds.overlaps(itemHitbox4)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("Scrapped Letter 2", "Scrapped letter 2.png");
                    item4PickedUp = true;
                    evidenceToShow = 4;
                    evidenceCooldown = 0f;
                    showPickupPrompt = false;
                    state = State.EVIDENCE;
                }
            }

            if (player.x <= -275) { currentRoom = 1; player.x = 850; }
            if (player.x > 1100) {
                game.setScreen(new GameScreen(game, true, returnX, returnY, 1));
            }
        }
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose(); stage.dispose(); inventory.dispose(); player.dispose();
        roomPt1.dispose(); roomPt2.dispose(); pauseBg.dispose();
        resumeTex.dispose(); quitTex.dispose(); exitSign.dispose();
        pickupPrompt.dispose();
        evidenceTex1.dispose(); evidenceTex2.dispose();
        evidenceTex3.dispose(); evidenceTex4.dispose();
        entryDialogue.dispose(); postEvidence1Dialogue.dispose();
        postEvidence2Dialogue.dispose(); postEvidence3Dialogue.dispose();
        postEvidence4Dialogue.dispose();
    }
}