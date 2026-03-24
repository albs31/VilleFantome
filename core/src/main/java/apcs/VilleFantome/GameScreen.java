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

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        player = new Player(600, 20);

        dialogueScreens = new Texture[] {
            new Texture("Narrator_box1.png"),      
            new Texture("Theo_Diary_Entry1.png"),   
            new Texture("Narrator_box2.png"),       
            new Texture("Theo_dialogue_2.png"),     
            new Texture("Theo_dialogue_1.png"),     
            new Texture("Game_controls.png"),    
            new Texture("Narrator_box3.png")    
        };

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

        resumeButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(resumeTex)));
        resumeButton.setPosition(540, 400);
        resumeButton.setSize(200, 80);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                state = State.RUNNING;
                typeSound.resume();
            }
        });

        quitButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(quitTex)));
        quitButton.setPosition(540, 280);
        quitButton.setSize(200, 80);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            showControls = !showControls;
        }

        if (fadeAlpha > 0) {
            fadeAlpha -= delta * fadeSpeed;
            if (fadeAlpha < 0) fadeAlpha = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            if (state == State.PAUSED) typeSound.pause(); else typeSound.resume();
        }

        if (state == State.RUNNING) updateGame(delta);

        if (!inputSet) {
            Gdx.input.setInputProcessor(stage);
            inputSet = true;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        if (currentDialogueIndex >= 7) { 
            batch.draw(backgroundTexture, 0, 0, 1280, 720);
            player.draw(batch); 
        }

        if (showControls) {
            batch.draw(dialogueScreens[5], 0, 0, 1280, 720); 
        } else if (currentDialogueIndex < dialogueScreens.length) {
            batch.draw(dialogueScreens[currentDialogueIndex], 0, 0, 1280, 720);
        }

        if (state == State.PAUSED) batch.draw(pauseBg, 0, 0, 1280, 720);
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
        } else {
            player.update(delta);
        }
    }

    private void resetDialogueEffects() {
        fadeAlpha = 1.0f;
        typeSound.stop();
        soundTimer = 0.0F;
        if (currentDialogueIndex < dialogueHasSound.length && dialogueHasSound[currentDialogueIndex]) {
            typeSound.play(1.0F);
            soundPlaying = true;
        } else {
            soundPlaying = false;
        }
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); inputSet = false; }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose(); stage.dispose(); backgroundTexture.dispose();
        typeSound.dispose(); pauseBg.dispose();
        resumeTex.dispose(); quitTex.dispose();
        for (Texture t : dialogueScreens) t.dispose();
        player.dispose();
    }
}