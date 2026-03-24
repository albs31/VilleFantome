package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
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

public class GameScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Stage stage;
    private boolean inputSet = false;
    private Player player;

    // pause menu variables
    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;
    private Texture pauseBg;
    private Texture resumeTex;
    private Texture quitTex;
    private ImageButton resumeButton;
    private ImageButton quitButton;

    // dialogue variables
    private Texture[] dialogueScreens;
    private boolean[] dialogueHasSound; // Must match dialogueScreens length
    private int currentDialogueIndex = 0;
    private Sound typeSound;
    private float soundTimer = 0.0F;
    private boolean soundPlaying = false;

    // world variables
    private Texture backgroundTexture;

    // Fade variables
    private float fadeAlpha = 1.0f; 
    private float fadeSpeed = 3.5f; 

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));

        // Create the player using the Player class
        player = new Player(600, 20);

        // dialogue assets (Total: 6 screens)
        dialogueScreens = new Texture[] {
            new Texture("Narrator_box1.png"),      // 0
            new Texture("Theo_Diary_Entry1.png"),   // 1
            new Texture("Narrator_box2.png"),       // 2
            new Texture("Theo_dialogue_2.png"),     // 3
            new Texture("Theo_dialogue_1.png"),     // 4
            new Texture("Game_controls.png")        // 5
        };

        // sound logic (Must have 6 booleans)
        dialogueHasSound = new boolean[] { true, false, true, false, false, false };

        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));

        // Start initial sound if necessary
        if (dialogueHasSound[0]) {
            typeSound.play(1.0F);
            soundPlaying = true;
        }

        // world assets
        backgroundTexture = new Texture("background1.png");

        // pause menu assets
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");

        resumeButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(resumeTex)));
        resumeButton.setPosition(540, 400);
        resumeButton.setSize(200, 80);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (state == State.PAUSED) {
                    state = State.RUNNING;
                    typeSound.resume();
                }
            }
        });

        quitButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(quitTex)));
        quitButton.setPosition(540, 280);
        quitButton.setSize(200, 80);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (state == State.PAUSED) {
                    game.setScreen(new LoadingScreen(game));
                }
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);
        inputSet = false;
    }

    @Override
    public void render(float delta) {
        // 1. Update Fade Logic
        if (fadeAlpha > 0) {
            fadeAlpha -= delta * fadeSpeed;
            if (fadeAlpha < 0) fadeAlpha = 0;
        }

        // 2. ESCAPE/PAUSE
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (state == State.RUNNING) {
                state = State.PAUSED;
                typeSound.pause();
            } else {
                state = State.RUNNING;
                typeSound.resume();
            }
        }

        if (state == State.RUNNING) {
            updateGame(delta);
        }

        if (!inputSet) {
            Gdx.input.setInputProcessor(stage);
            inputSet = true;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        // LAYER 1: The Town (Shows after Game_controls at Index 5 is done)
        if (currentDialogueIndex >= 6) { 
            batch.draw(backgroundTexture, 0, 0, 1280, 720);
            player.draw(batch); 
        }

        // LAYER 2: Dialogue Overlay
        if (currentDialogueIndex < dialogueScreens.length) {
            batch.draw(dialogueScreens[currentDialogueIndex], 0, 0, 1280, 720);
        }

        // LAYER 3: Menus
        if (state == State.PAUSED) {
            batch.draw(pauseBg, 0, 0, 1280, 720);
        }

        // LAYER 4: Fade
        if (fadeAlpha > 0) {
            batch.setColor(0, 0, 0, fadeAlpha); 
            batch.draw(pauseBg, 0, 0, 1280, 720); 
            batch.setColor(1, 1, 1, 1); 
        }

        batch.end();

        if (state == State.PAUSED) {
            stage.act(delta);
            stage.draw();
        }
    }

    private void updateGame(float delta) {
    // 1. SOUND TIMER LOGIC
    if (soundPlaying) {
        soundTimer += delta;
        if (soundTimer >= 4.0F) { 
            typeSound.stop(); 
            soundPlaying = false; 
        }
    }

    // 2. DIALOGUE CONTROLS (Only runs if we haven't finished the intro)
    if (currentDialogueIndex < dialogueScreens.length) {
        
        // --- GO FORWARD (Right Arrow or Click) ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.justTouched()) {
            advanceDialogue();
        }
        
        // --- GO BACKWARD (Left Arrow) ---
        else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (currentDialogueIndex > 0) {
                currentDialogueIndex--;
                resetDialogueEffects();
            }
        }
    }

    // 3. MOVEMENT LOGIC (Only runs when ALL dialogue is finished)
    else { 
        player.update(delta);
    }
}

/** * Helper method to handle moving forward and resetting sounds/fades
 */
private void advanceDialogue() {
    currentDialogueIndex++; 
    resetDialogueEffects();
}

/**
 * Resets the fade and sound whenever we change screens
 */
private void resetDialogueEffects() {
    fadeAlpha = 1.0f;
    typeSound.stop();
    soundTimer = 0.0F;

    // Check if the NEW index should play sound
    if (currentDialogueIndex < dialogueHasSound.length && dialogueHasSound[currentDialogueIndex]) {
        typeSound.play(1.0F);
        soundPlaying = true;
    } else {
        soundPlaying = false; 
    }
}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        inputSet = false;
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        backgroundTexture.dispose();
        pauseBg.dispose();
        typeSound.dispose();
        resumeTex.dispose();
        quitTex.dispose();
        
        for (Texture t : dialogueScreens) {
            if (t != null) t.dispose();
        }
        player.dispose(); // Important: dispose textures inside Player class too
    }
}