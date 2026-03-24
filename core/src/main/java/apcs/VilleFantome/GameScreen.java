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

public class GameScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private Player player;
    private boolean showControls = false;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private Texture pauseBg, resumeTex, quitTex;
    private ImageButton resumeButton, quitButton;

    private Texture[] dialogueScreens;
    private boolean[] dialogueHasSound;
    private int currentDialogueIndex = 0;

    private Sound typeSound;
    private float soundTimer = 0.0F;
    private boolean soundPlaying = false;

    private Texture backgroundTexture;
    private float fadeAlpha = 1.0f;
    private float fadeSpeed = 3.5f;

    private Texture enterSign;
    private Rectangle door1Bounds, door2Bounds, playerBounds;
    private boolean showEnterSign = false;
    private int currentDoor = 0;

    // Movement Delay Variables
    private float movementDelayTimer = 0.0f;
    private final float MAX_DELAY = 0.8f; // Small pause before walking

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        
        // Start with stage as input processor so buttons work if paused immediately
        Gdx.input.setInputProcessor(stage);

        player = new Player(10, 20);

        dialogueScreens = new Texture[] {
            new Texture("Narrator_box1.png"),      // 0
            new Texture("Theo_Diary_Entry1.png"),   // 1
            new Texture("Narrator_box2.png"),       // 2
            new Texture("Theo_dialogue_2.png"),     // 3
            new Texture("Theo_dialogue_1.png"),     // 4
            new Texture("Game_controls.png"),       // 5
            new Texture("Objective1.png")           // 6
        };

        // Must match length of dialogueScreens (7)
        dialogueHasSound = new boolean[] { true, false, true, false, false, false, false };

        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        if (dialogueHasSound[0]) {
            typeSound.play(1.0F);
            soundPlaying = true;
        }

        backgroundTexture = new Texture("background1.png");
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        enterSign = new Texture("entersign.png");

        door1Bounds = new Rectangle(90, 120, 110, 180);
        door2Bounds = new Rectangle(830, 120, 110, 180);
        playerBounds = new Rectangle();

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
                typeSound.resume();
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
        // 1. INPUT & LOGIC
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) showControls = !showControls;

        if (fadeAlpha > 0) {
            fadeAlpha -= delta * fadeSpeed;
            if (fadeAlpha < 0) fadeAlpha = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            if (state == State.PAUSED) {
                Gdx.input.setInputProcessor(stage);
                typeSound.pause();
            } else {
                Gdx.input.setInputProcessor(null); // Return focus to game
                typeSound.resume();
            }
        }

        if (state == State.RUNNING) updateGame(delta);

        // 2. DRAWING
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        // LAYER 1: Gameplay (Only shows after dialogue is done)
        if (currentDialogueIndex >= dialogueScreens.length) {
            batch.draw(backgroundTexture, 0, 0, 1280, 720);
            player.draw(batch);

            if (showEnterSign) {
                // Draws original PNG size centered horizontally
                float xPos = (1280 / 2f) - (enterSign.getWidth() / 2f);
                float yPos = 400; // Vertical position
                batch.draw(enterSign, xPos, yPos);
            }
        } 
        // LAYER 2: Dialogue / Controls
        else {
            if (showControls) {
                batch.draw(dialogueScreens[5], 0, 0, 1280, 720);
            } else {
                batch.draw(dialogueScreens[currentDialogueIndex], 0, 0, 1280, 720);
            }
        }

        // LAYER 3: Pause Overlay
        if (state == State.PAUSED) {
            batch.draw(pauseBg, 0, 0, 1280, 720);
        }

        // LAYER 4: Global Fade
        if (fadeAlpha > 0) {
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(pauseBg, 0, 0, 1280, 720); // Using pauseBg as a black filler
            batch.setColor(1, 1, 1, 1);
        }

        batch.end();

        // 3. UI STAGE (Buttons)
        if (state == State.PAUSED) {
            stage.act(delta);
            stage.draw();
        }
    }
    
    private void updateGame(float delta) {
        if (showControls) return;

        // Sound Logic
        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer >= 4.0F) {
                typeSound.stop();
                soundPlaying = false;
            }
        }

        // 1. Dialogue Phase
        if (currentDialogueIndex < dialogueScreens.length) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                currentDialogueIndex++;
                resetDialogueEffects();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && currentDialogueIndex > 0) {
                currentDialogueIndex--;
                resetDialogueEffects();
            }
        } 
        // 2. Pause Phase (The "Small Pause" before moving)
        else if (movementDelayTimer < MAX_DELAY) {
            movementDelayTimer += delta;
        }
        // 3. Gameplay Phase
        else {
            player.update(delta);

            // Bounds logic - Adjusted to match Player getters
            playerBounds.set(player.x + 170, player.y + 40, 140, 260);

            showEnterSign = false;
            currentDoor = 0;

            if (playerBounds.overlaps(door1Bounds)) {
                showEnterSign = true;
                currentDoor = 1;
            } else if (playerBounds.overlaps(door2Bounds)) {
                showEnterSign = true;
                currentDoor = 2;
            }

            if (showEnterSign && Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                if (currentDoor == 1) game.setScreen(new House1Screen(game));
                else if (currentDoor == 2) game.setScreen(new House2Screen(game));
            }
        }
    }

    private void resetDialogueEffects() {
        fadeAlpha = 1.0f;
        typeSound.stop();
        soundTimer = 0.0F;

        // Sound array safety check
        if (currentDialogueIndex < dialogueHasSound.length && dialogueHasSound[currentDialogueIndex]) {
            typeSound.play(1.0F);
            soundPlaying = true;
        } else {
            soundPlaying = false;
        }
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        backgroundTexture.dispose();
        typeSound.dispose();
        pauseBg.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        enterSign.dispose();
        for (Texture t : dialogueScreens) t.dispose();
        player.dispose();
    }
}
