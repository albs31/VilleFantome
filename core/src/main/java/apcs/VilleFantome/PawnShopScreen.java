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

public class PawnShopScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private Player player;
    private Inventory inventory;
    private DialogueManager entryDialogue, postEvidenceDialogue, postEvidence2Dialogue;
    private DialogueManager activeDialogue = null;

    private enum State { WAITING, RUNNING, PAUSED, EVIDENCE, DIALOGUE }
    private State state = State.RUNNING;

    private Texture pawnShop1, pawnShop2, pauseBg, resumeTex, quitTex, exitSign;
    private Texture pickupPrompt, evidenceTex1, evidenceTex2;
    private ImageButton resumeButton, quitButton;

    private int currentRoom = 1;
    private Rectangle playerBounds, exitHitbox, itemHitbox1, itemHitbox2;
    private boolean showExitSign = false;
    private boolean showPickupPrompt = false;
    private boolean item1PickedUp = false;
    private boolean item2PickedUp = false;
    private int evidenceToShow = 0;

    private float movementDelayTimer = 0f;
    private final float MAX_DELAY = 0.04f;

    private float entryDialogueTimer = 0f;
    private final float ENTRY_DIALOGUE_DELAY = 2.0f;

    private float evidenceCooldown = 0f;

    private float returnX, returnY;

    // Static variables — survive screen transitions
    private static boolean hasVisitedBefore = false;
    private static boolean item1AlreadyPickedUp = false;
    private static boolean item2AlreadyPickedUp = false;

    public static void resetVisit() {
        hasVisitedBefore = false;
        item1AlreadyPickedUp = false;
        item2AlreadyPickedUp = false;
    }

    public PawnShopScreen(Main game, float x, float y) {
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

        pawnShop1 = new Texture("PawnShop1.png");
        pawnShop2 = new Texture("PawnShop2.png");
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        exitSign = new Texture("exitsign.png");
        pickupPrompt = new Texture("pickupitem.png");
        evidenceTex1 = new Texture("Evidence_In_Pawn_Shop.png");
        evidenceTex2 = new Texture("Diary Entry1.png");

        player = new Player(-300, -100); 
        currentRoom = 1;
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f);

        playerBounds = new Rectangle();
        exitHitbox = new Rectangle(1000, 0, 280, 720);
        itemHitbox1 = new Rectangle(600, 0, 120, 300);
        itemHitbox2 = new Rectangle(800, 0, 120, 300);

        entryDialogue = new DialogueManager(
            new String[] { "PawnShopDialogue1.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        postEvidenceDialogue = new DialogueManager(
            new String[] { "PawnShopDialogue2.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        postEvidence2Dialogue = new DialogueManager(
            new String[] { "PawnShopDialogue3.png" },
            new float[]  { 0f },
            () -> {
                activeDialogue = null;
                state = State.RUNNING;
            }
        );

        // Restore pickup state from previous visit
        item1PickedUp = item1AlreadyPickedUp;
        item2PickedUp = item2AlreadyPickedUp;

        // Skip entry dialogue if already visited
        if (hasVisitedBefore) {
            state = State.RUNNING;
        } else {
            state = State.WAITING;
        }

        hasVisitedBefore = true;

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
                SaveManager.save(returnX, returnY, 1, 7, "pawnshop");
                game.setScreen(new LoadingScreen(game));
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
            if (evidenceCooldown > 0.1f && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                evidenceCooldown = 0f;
                if (evidenceToShow == 1) startDialogue(postEvidenceDialogue);
                if (evidenceToShow == 2) startDialogue(postEvidence2Dialogue);
            }
        } else if (state == State.DIALOGUE) {
            // handled internally by DialogueManager
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

        batch.draw(currentRoom == 1 ? pawnShop1 : pawnShop2, 0, 0, 1280, 720);
        player.draw(batch);

        if (currentRoom == 2 && showExitSign) batch.draw(exitSign, 0, 0, 1280, 720);
        if (showPickupPrompt) batch.draw(pickupPrompt, 0, 0, 1280, 720);

        if (state == State.EVIDENCE) {
            if (evidenceToShow == 1) batch.draw(evidenceTex1, 0, 0, 1280, 720);
            if (evidenceToShow == 2) batch.draw(evidenceTex2, 0, 0, 1280, 720);
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
                    Inventory.addItem("Pocketwatch", "Evidence_In_Pawn_Shop.png");
                    item1PickedUp = true;
                    item1AlreadyPickedUp = true;
                    evidenceToShow = 1;
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
            if (!item2PickedUp && playerBounds.overlaps(itemHitbox2)) {
                showPickupPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    Inventory.addItem("Diary Entry 2", "Diary Entry1.png");
                    item2PickedUp = true;
                    item2AlreadyPickedUp = true;
                    evidenceToShow = 2;
                    evidenceCooldown = 0f;
                    showPickupPrompt = false;
                    state = State.EVIDENCE;
                }
            }
            if (player.x <= -275) { currentRoom = 1; player.x = 850; }
            if (player.x > 900) game.setScreen(new GameScreen(game, true, returnX, returnY, 1));
        }
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose(); stage.dispose(); inventory.dispose(); player.dispose();
        pawnShop1.dispose(); pawnShop2.dispose(); pauseBg.dispose();
        resumeTex.dispose(); quitTex.dispose(); exitSign.dispose();
        pickupPrompt.dispose(); evidenceTex1.dispose(); evidenceTex2.dispose();
        entryDialogue.dispose(); postEvidenceDialogue.dispose(); postEvidence2Dialogue.dispose();
    }
}
