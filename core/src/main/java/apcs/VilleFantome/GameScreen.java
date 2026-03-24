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
    private boolean[] dialogueHasSound;
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
    // Replace dialogue1, dialogue2, etc. with this:
    private Texture[] dialogueScreens;
    private int currentDialogueIndex = 0;
    private Sound typeSound;
    private float soundTimer = 0.0F;
    private boolean soundPlaying = true;

    // world and player variables
    private Texture backgroundTexture;
    private Texture playerIdle;
    private Texture[] leftFrames;
    private Texture[] rightFrames;
    private Texture currentPlayerTexture;
    private float playerX = 600.0F;
    private float playerY = 20.0F;
    private float playerSpeed = 300.0F;
    private float animationTimer = 0.0F;
    private float frameDuration = 0.15F;

    // Fade variables
    private float fadeAlpha = 1.0f; // Start fully black
    private float fadeSpeed = 3.5f; // Adjust this to make it faster or slower

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        stage = new Stage(new FitViewport(1280, 720));

        player = new Player(600, 20);

        // dialogue assets
        // List them in order here. You can add 100 here and the logic won't change!
        dialogueScreens = new Texture[] {
        new Texture("Narrator_box1.png"),
        new Texture ("Theo_Diary_Entry1.png"),
        new Texture("Narrator_box2.png"), // add dialogue boxes here :)

         
        };
        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        typeSound.play(1.0F);
        
        dialogueHasSound = new boolean[] {
        true,  // Narrator 1 -> Sound ON
        false, // Letter 2   -> Sound OFF
        true,  // Narrator 2 -> Sound ON
        false   // Narrator 3 -> Sound ON
    };

    // Start the first sound ONLY if the first screen is a narrator box
    if (dialogueHasSound[0]) {
        typeSound.play(1.0F);
        soundPlaying = true;
    } else {
        soundPlaying = false;
    }

        // world assets
        backgroundTexture = new Texture("background1.png");
        playerIdle = new Texture("standing_still.png");

        // pause menu assets
        pauseBg = new Texture("pause_screen.png");

        resumeTex = new Texture("resume_button.png");
        resumeButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(resumeTex))
        );
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

        quitTex = new Texture("quitbutton.png");
        quitButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(quitTex))
        );
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

        // animation frames for player moving
        leftFrames = new Texture[]{new Texture("left(1).png"), new Texture("left(2).png"), new Texture("left(3).png")};
        rightFrames = new Texture[]{new Texture("right(1).png"), new Texture("right(2).png"), new Texture("right(3).png")};
        currentPlayerTexture = playerIdle;

        inputSet = false;
    }

    @Override
public void render(float delta) {
    // 1. Update Fade Logic (Must happen every frame)
    if (fadeAlpha > 0) {
        fadeAlpha -= delta * fadeSpeed;
        if (fadeAlpha < 0) fadeAlpha = 0;
    }

    // 2. ESCAPE/PAUSE COMMAND
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        if (state == State.RUNNING) {
            state = State.PAUSED;
            typeSound.pause();
        } else {
            state = State.RUNNING;
            typeSound.resume();
        }
    }

    // 3. GAME LOGIC (Only runs when not paused)
    if (state == State.RUNNING) {
        updateGame(delta);
    }

    // 4. INPUT PROCESSOR SETUP
    if (!inputSet) {
        Gdx.input.setInputProcessor(stage);
        inputSet = true;
    }

    // 5. DRAWING
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch.setProjectionMatrix(stage.getCamera().combined);
    
    // --- START BATCH ---
    batch.begin();

    // 1. LAYER 1: The Town (Background + Player)
    // We start drawing the town as soon as we hit Box 3
    if (currentDialogueIndex >= 2) { 
        batch.draw(backgroundTexture, 0, 0, 1280, 720);
        player.draw(batch); 
    }

    // 2. LAYER 2: The Dialogue Overlay
    // This draws whatever box we are currently on
    if (currentDialogueIndex < dialogueScreens.length) {
        batch.draw(dialogueScreens[currentDialogueIndex], 0, 0, 1280, 720);
    }

    // 3. LAYER 3: Menus & Transitions
    // Draw Pause Tint
    if (state == State.PAUSED) {
        batch.draw(pauseBg, 0, 0, 1280, 720);
    }

    // Draw Fade Transition (Must be the absolute last thing in the batch)
    if (fadeAlpha > 0) {
        batch.setColor(0, 0, 0, fadeAlpha); 
        batch.draw(pauseBg, 0, 0, 1280, 720); 
        batch.setColor(1, 1, 1, 1); 
    }

    batch.end();

    // 6. DRAW STAGE (Buttons)
    // Stage has its own internal batch.begin/end, so it stays outside yours.
    if (state == State.PAUSED) {
        stage.act(delta);
        stage.draw();
    }
}
private void updateGame(float delta) {
        // 1. SOUND TIMER (Stops sound after 4 seconds)
        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer >= 4.0F) { 
                typeSound.stop(); 
                soundPlaying = false; 
            }
        }

        // 2. DIALOGUE CLICK LOGIC
        if (currentDialogueIndex < dialogueScreens.length) {
            if (Gdx.input.justTouched()) {
                currentDialogueIndex++; 

                fadeAlpha = 1.0f; // This triggers the fade for the NEW screen
                
                // Reset for the next screen
                typeSound.stop();
                soundTimer = 0.0F;

                // Should this new index play a sound?
                if (currentDialogueIndex < dialogueScreens.length && dialogueHasSound[currentDialogueIndex]) {
                    typeSound.play(1.0F);
                    soundPlaying = true;
                } else {
                    soundPlaying = false; 
                }
            }
        }

        // 3. MOVEMENT LOGIC (Only runs when dialogue is finished)
        if (currentDialogueIndex >= dialogueScreens.length) { 
        
        // This one line replaces all your old movement IF statements!
        player.update(delta);
    } // End of updateGame
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
    playerIdle.dispose();
    pauseBg.dispose();
    typeSound.dispose();
    
    // Dispose all dialogue textures in the array
    for (Texture t : dialogueScreens) {
        if (t != null) t.dispose();
    }
    
    for (Texture t : leftFrames) t.dispose();
    for (Texture t : rightFrames) t.dispose();
}
}
