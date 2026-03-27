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
    private Inventory inventory;
    private SpriteBatch batch;
    private Stage stage;
    private Player player;
    private boolean showControls = false;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private Texture pauseBg, resumeTex, quitTex, enterSign;
    private Texture background1, background2;
    
    private int currentArea = 1; 
    private int returnArea;      

    private ImageButton resumeButton, quitButton;
    private Texture[] dialogueScreens;
    private boolean[] dialogueHasSound;
    private int currentDialogueIndex = 0;

    private Sound typeSound;
    private float soundTimer = 0.0f;
    private boolean soundPlaying = false;

    private float fadeAlpha = 1.0f;
    private float fadeSpeed = 3.5f;

    private Rectangle door1Bounds, door2Bounds, player3Bounds, playerBounds; 
    private boolean showEnterSign = false;

    private float movementDelayTimer = 0.0f;
    private final float MAX_DELAY = 1.0f;
    private boolean isReturning;
    
    private float spawnX, spawnY;

    // CONSTRUCTOR FIX: 5 Arguments total
    public GameScreen(Main game, boolean isReturning, float x, float y, int returnArea) {
        this.game = game;
        this.isReturning = isReturning;
        this.spawnX = x;
        this.spawnY = y;
        this.returnArea = returnArea; 
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        inventory = new Inventory();
        Gdx.input.setInputProcessor(stage);

        background1 = new Texture("background1.png");
        background2 = new Texture("townpart_2.png");
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");
        enterSign = new Texture("entersign.png");

        dialogueScreens = new Texture[] {
            new Texture("Narrator_box1.png"), new Texture("Theo_Diary_Entry1.png"),
            new Texture("Narrator_box2.png"), new Texture("Theo_dialogue_2.png"),
            new Texture("Theo_dialogue_1.png"), new Texture("Objective1.png"), 
            new Texture("Game_controls.png")
        };

        dialogueHasSound = new boolean[] { true, false, true, false, false, false, false };
        typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));

        // CRITICAL FIX: Create player immediately
        player = new Player(spawnX, spawnY); 

        if (isReturning) {
            currentDialogueIndex = dialogueScreens.length; 
            this.currentArea = returnArea; 
            movementDelayTimer = 0.0f; 
            fadeAlpha = 1.0f; 
        } else {
            currentArea = 1;
            player.x = 10;
            player.y = 20;
        }

        door1Bounds = new Rectangle(930, 10, 10, 180); 
        door2Bounds = new Rectangle(1230, 10, 10, 180); 
        player3Bounds = new Rectangle(390, 10, 40, 100); 
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
                Gdx.input.setInputProcessor(null);
            }
        });

        quitButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(quitTex)));
        quitButton.setPosition(500, 200);
        quitButton.setSize(300, 120);
        quitButton.addListener(new ClickListener() {
    @Override
    public void clicked(InputEvent event, float x, float y) {
        SaveManager.save(player.x, player.y, currentArea, currentDialogueIndex);
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
            Gdx.input.setInputProcessor(state == State.PAUSED ? stage : null);
            if (state == State.PAUSED) typeSound.pause(); else typeSound.resume();
        }

        if (state == State.RUNNING) updateGame(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        if (currentDialogueIndex < dialogueScreens.length) {
            Texture currentTex = showControls ? dialogueScreens[5] : dialogueScreens[currentDialogueIndex];
            batch.draw(currentTex, 0, 0, 1280, 720);
        } else {
            batch.draw(currentArea == 1 ? background1 : background2, 0, 0, 1280, 720);
            if (player != null) player.draw(batch); 
            if (showEnterSign) batch.draw(enterSign, 0, 0, 1280, 720); 
        }

        if (state == State.PAUSED) batch.draw(pauseBg, 0, 0, 1280, 720);

        if (fadeAlpha > 0) {
            batch.setColor(0, 0, 0, fadeAlpha);
            batch.draw(pauseBg, 0, 0, 1280, 720);
            batch.setColor(1, 1, 1, 1);
        }
        batch.end();

        inventory.handleInput();
        inventory.render(delta);

        if (state == State.PAUSED) { stage.act(delta); stage.draw(); }
    }

    private void updateGame(float delta) {
        if (showControls || player == null) return;

        if (soundPlaying) {
            soundTimer += delta;
            if (soundTimer >= 4.0f) { 
                typeSound.stop(); 
                soundPlaying = false; 
            }
        }

        if (currentDialogueIndex < dialogueScreens.length) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                currentDialogueIndex++;
                resetDialogueEffects();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && currentDialogueIndex > 0) {
                currentDialogueIndex--;
                resetDialogueEffects();
            }
        } else if (movementDelayTimer < MAX_DELAY) {
            movementDelayTimer += delta;
        } else {
            player.update(delta);
            playerBounds.set(player.x + 470, player.y + 40, 60, 400);

            if (currentArea == 1 && player.x > 1275) {
                currentArea = 2; 
                player.x = -100;
            } else if (currentArea == 2 && player.x < -160) {
                currentArea = 1; 
                player.x = 1200;
            }

            showEnterSign = false;

            if (currentArea == 1) {
                if (playerBounds.overlaps(door1Bounds)) {
                    showEnterSign = true;
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) 
                        game.setScreen(new PawnShopScreen(game, player.x, player.y));
                }
                if (playerBounds.overlaps(door2Bounds)) {
                    showEnterSign = true;
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) 
                        game.setScreen(new PreviousRoomScreen(game, player.x, player.y));
                }
            } 
            else if (currentArea == 2) {
                if (playerBounds.overlaps(player3Bounds)) {
                    showEnterSign = true;
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) 
                        game.setScreen(new JHouseScreen(game, player.x, player.y));
                }
            }
        }
    }

    private void resetDialogueEffects() {
        fadeAlpha = 1.0f;
        typeSound.stop();
        soundTimer = 0.0f;
        if (currentDialogueIndex < dialogueHasSound.length && dialogueHasSound[currentDialogueIndex]) {
            typeSound.play(1.0f);
            soundPlaying = true;
        } else { 
            soundPlaying = false; 
        }
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose(); stage.dispose(); background1.dispose(); background2.dispose();
        typeSound.dispose(); pauseBg.dispose(); resumeTex.dispose();
        quitTex.dispose(); enterSign.dispose(); inventory.dispose();
        for (Texture t : dialogueScreens) t.dispose();
        if (player != null) player.dispose();
    }
}