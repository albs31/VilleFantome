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

    private Texture pauseBg, resumeTex, quitTex, enterSign;
    private Texture background1, background2;
    private int currentArea = 1; 

    private ImageButton resumeButton, quitButton;
    private Texture[] dialogueScreens;
    private boolean[] dialogueHasSound;
    private int currentDialogueIndex = 0;

    private Sound typeSound;
    private float soundTimer = 0.0F;
    private boolean soundPlaying = false;

    private float fadeAlpha = 1.0f;
    private float fadeSpeed = 3.5f;

    private Rectangle door1Bounds, door2Bounds, playerBounds;
    private boolean showEnterSign = false;
    private int currentDoor = 0;

    private float movementDelayTimer = 0.0f;
    private final float MAX_DELAY = 1.0f; 
    private boolean isReturning;

    public GameScreen(Main game, boolean isReturning) {
        this.game = game;
        this.isReturning = isReturning;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        Gdx.input.setInputProcessor(stage);
        
        fadeAlpha = 1.0f; 

        background1 = new Texture("background1.png");
        background2 = new Texture("townpart_2.png");
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        enterSign = new Texture("entersign.png");

        dialogueScreens = new Texture[] {
            new Texture("Narrator_box1.png"), new Texture("Theo_Diary_Entry1.png"),
            new Texture("Narrator_box2.png"), new Texture("Theo_dialogue_2.png"),
            new Texture("Theo_dialogue_1.png"), new Texture("Game_controls.png"),
            new Texture("Objective1.png")
        };

        dialogueHasSound = new boolean[] { true, false, true, false, false, false, false };
        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));

        if (isReturning) {
            currentDialogueIndex = dialogueScreens.length; 
            player = new Player(1100, 20); 
            movementDelayTimer = MAX_DELAY; // Skips the pause when returning
        } else {
            currentDialogueIndex = 0;
            player = new Player(10, 20);
            movementDelayTimer = 0.0f; // Triggers intro pause
            if (dialogueHasSound[0]) {
                typeSound.play(1.0F);
                soundPlaying = true;
            }
        }

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
        quitButton.setPosition(500, 300); 
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) showControls = !showControls;
        if (fadeAlpha > 0) fadeAlpha = Math.max(0, fadeAlpha - delta * fadeSpeed);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            if (state == State.PAUSED) { Gdx.input.setInputProcessor(stage); typeSound.pause(); }
            else { Gdx.input.setInputProcessor(null); typeSound.resume(); }
        }

        if (state == State.RUNNING) updateGame(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        if (currentDialogueIndex >= dialogueScreens.length) {
            batch.draw(currentArea == 1 ? background1 : background2, 0, 0, 1280, 720);
            player.draw(batch);
            if (showEnterSign) {
                batch.draw(enterSign, (1280 / 2f) - (enterSign.getWidth() / 2f), 400);
            }
        } else {
            batch.draw(showControls ? dialogueScreens[5] : dialogueScreens[currentDialogueIndex], 0, 0, 1280, 720);
        }

        if (state == State.PAUSED) batch.draw(pauseBg, 0, 0, 1280, 720);

        if (fadeAlpha > 0) {
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(pauseBg, 0, 0, 1280, 720);
            batch.setColor(1, 1, 1, 1);
        }
        batch.end();

        if (state == State.PAUSED) { stage.act(delta); stage.draw(); }
    }

    private void updateGame(float delta) {
        if (showControls) return;

        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer >= 4.0F) { typeSound.stop(); soundPlaying = false; }
        }

        if (currentDialogueIndex < dialogueScreens.length) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                currentDialogueIndex++;
                resetDialogueEffects();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && currentDialogueIndex > 0) {
                currentDialogueIndex--;
                resetDialogueEffects();
            }
        } 
        else if (movementDelayTimer < MAX_DELAY) {
            movementDelayTimer += delta;
            
            // Auto-walk logic for entering screens
            if (currentArea == 2 && player.x < 50) {
                player.x += 250 * delta; 
            } else if (currentArea == 1 && player.x > 1100) {
                player.x -= 250 * delta;
            }
        } 
        else {
            player.update(delta);

            // FIXED AREA SWITCHING: Reset timer to MAX_DELAY - 0.2 to allow tiny auto-walk but NO full pause
            if (currentArea == 1 && player.x > 1275) {
                currentArea = 2;
                player.x = -150; 
                movementDelayTimer = MAX_DELAY - 0.2f; // Slight auto-walk, no story pause
            } 
            else if (currentArea == 2 && player.x < -160) {
                currentArea = 1;
                player.x = 1300; 
                movementDelayTimer = MAX_DELAY - 0.2f; // Slight auto-walk, no story pause
            }

            showEnterSign = false;
            if (currentArea == 1) {
                playerBounds.set(player.x + 170, player.y + 40, 140, 260);
                if (playerBounds.overlaps(door1Bounds)) {
                    showEnterSign = true;
                    currentDoor = 1;
                } else if (playerBounds.overlaps(door2Bounds)) {
                    showEnterSign = true;
                    currentDoor = 2;
                }

                if (showEnterSign && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    if (currentDoor == 1) game.setScreen(new House1Screen(game));
                    else if (currentDoor == 2) game.setScreen(new House2Screen(game));
                }
            }
        }
    }

    private void resetDialogueEffects() {
        fadeAlpha = 1.0f;
        typeSound.stop();
        soundTimer = 0.0F;
        if (currentDialogueIndex < dialogueHasSound.length && dialogueHasSound[currentDialogueIndex]) {
            typeSound.play(1.0F);
            soundPlaying = true;
        } else { soundPlaying = false; }
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose(); stage.dispose(); background1.dispose(); background2.dispose();
        typeSound.dispose(); pauseBg.dispose(); resumeTex.dispose();
        quitTex.dispose(); enterSign.dispose();
        for (Texture t : dialogueScreens) t.dispose();
        player.dispose();
    }
}