package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture dialogue1;
    private Texture dialogue2;
    private Texture currentNarratorBox;
    private Sound typeSound;

    private int sceneState = 1;
    private float soundTimer = 0.0F;
    private boolean soundPlaying = true;

   
    private Texture backgroundTexture;
    private Texture playerIdle;
    private Texture[] leftFrames;
    private Texture[] rightFrames;
    private Texture currentPlayerTexture;

   
    private float playerX;
    private float playerY;
    private float playerSpeed = 300.0F;

   
    private float animationTimer = 0.0F;
    private float frameDuration = 0.15F;

    public GameScreen(Main game) {
        this.game = game;
    }

    public void show() {
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(1280.0F, 720.0F, this.camera);
        this.viewport.apply();
        this.camera.position.set(640.0F, 360.0F, 0.0F);

        this.dialogue1 = new Texture("Narrator_box1.png");
        this.dialogue2 = new Texture("scrapped letter 3.png");
        this.currentNarratorBox = this.dialogue1;
        this.typeSound = Gdx.audio.newSound(Gdx.files.internal("NarratorTypeSound.mp3"));
        this.typeSound.play(1.0F);

       
        this.backgroundTexture = new Texture("background1.png");
        this.playerIdle = new Texture("standing_still.png");

        this.leftFrames = new Texture[3];
        this.leftFrames[0] = new Texture("left(1).png");
        this.leftFrames[1] = new Texture("left(2).png");
        this.leftFrames[2] = new Texture("left(3).png");

        this.rightFrames = new Texture[3];
        this.rightFrames[0] = new Texture("right(1).png");
        this.rightFrames[1] = new Texture("right(2).png");
        this.rightFrames[2] = new Texture("right(3).png");

        this.currentPlayerTexture = this.playerIdle;

        // where the player starts
        this.playerX = 600.0F;
        this.playerY = 20.0F;
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (this.soundPlaying) {
            this.soundTimer += delta;
            if (this.soundTimer > 4.0F) {
                this.typeSound.stop();
                this.soundPlaying = false;
            }
        }

        if (Gdx.input.justTouched()) {
            if (this.sceneState == 1) {
                this.sceneState = 2;
                this.currentNarratorBox = this.dialogue2;
                this.typeSound.stop();
                this.soundPlaying = false;
            } else if (this.sceneState == 2) {
                this.sceneState = 3;
            }
        }

       
        if (this.sceneState == 3) {
            boolean moving = false;

            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
                this.playerX -= this.playerSpeed * delta;
                this.animationTimer += delta;

                int frameIndex = (int)(this.animationTimer / this.frameDuration) % this.leftFrames.length;
                this.currentPlayerTexture = this.leftFrames[frameIndex];
                moving = true;
            }

            if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
                this.playerX += this.playerSpeed * delta;
                this.animationTimer += delta;

                int frameIndex = (int)(this.animationTimer / this.frameDuration) % this.rightFrames.length;
                this.currentPlayerTexture = this.rightFrames[frameIndex];
                moving = true;
            }

            if (!moving) {
                this.currentPlayerTexture = this.playerIdle;
                this.animationTimer = 0.0F;
            }

            // player on screen lol
           this.playerX = Math.max(0.0F, Math.min(this.playerX, 1280.0F - 500.0F));
        }

        this.camera.update();
        this.batch.setProjectionMatrix(this.camera.combined);
        this.batch.begin();

        
        if (this.sceneState == 3) {
            this.batch.draw(this.backgroundTexture, 0.0F, 0.0F, 1280.0F, 720.0F);
        }

        
        if (this.sceneState < 3) {
            this.batch.draw(this.currentNarratorBox, 0.0F, 0.0F, 1280.0F, 720.0F);
        }

        
        if (this.sceneState == 3) {
            this.batch.draw(this.currentPlayerTexture, this.playerX, this.playerY, 550.0F, 400.0F);
        }

        this.batch.end();
    }

    public void resize(int width, int height) {
        if (this.viewport != null) {
            this.viewport.update(width, height, true);
        }
    }

    public void dispose() {
        this.batch.dispose();
        this.dialogue1.dispose();
        this.dialogue2.dispose();

        this.backgroundTexture.dispose();
        this.playerIdle.dispose();

        for (Texture frame : this.leftFrames) {
            frame.dispose();
        }

        for (Texture frame : this.rightFrames) {
            frame.dispose();
        }

        if (this.typeSound != null) {
            this.typeSound.dispose();
        }
    }

    public void hide() {
    }

    public void pause() {
    }

    public void resume() {
    }
}
