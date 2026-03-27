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

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private Texture pawnShop1, pawnShop2, pauseBg, resumeTex, quitTex, exitSign;
    private ImageButton resumeButton, quitButton;

    private int currentRoom = 1;
    private Rectangle playerBounds, exitHitbox;
    private boolean showExitSign = false;
    
    private float movementDelayTimer = 0f;
    private final float MAX_DELAY = 0.04f;
    
    private float returnX, returnY;

    public PawnShopScreen(Main game, float x, float y) {
        this.game = game;
        this.returnX = x;
        this.returnY = y;
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

        // Spawn on the left side of the first room
        player = new Player(20, -100); 
        currentRoom = 1; 
        
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f); 

        playerBounds = new Rectangle();
        
        // Hitbox for the exit sign: only the leftmost 20 pixels
        exitHitbox = new Rectangle(1000, 0, 280, 720); 

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
                game.setScreen(new GameScreen(game, true, returnX, returnY));
            }
        });

        stage.addActor(resumeButton);
        stage.addActor(quitButton);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            state = (state == State.RUNNING) ? State.PAUSED : State.RUNNING;
            Gdx.input.setInputProcessor(state == State.PAUSED ? stage : null);
        }

        if (state == State.RUNNING) updateGame(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        
        // Draw background based on current room
        batch.draw(currentRoom == 1 ? pawnShop1 : pawnShop2, 0, 0, 1280, 720);
        
        player.draw(batch);

        // Layer the exit sign over the entire screen if triggered in Room 2
        if (currentRoom == 2 && showExitSign) {
            batch.draw(exitSign, 0, 0, 1280, 720); 
        }

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

        if (currentRoom == 1) {
            if (player.x < -300) player.x = -300; 
            
            // GO TO ROOM 2
            if (player.x >= 1100) { 
                currentRoom = 2;
                player.x = -270;    
            }
        } 
        else if (currentRoom == 2) {
            // SHOW EXIT SIGN (When near the right side)
            if (playerBounds.overlaps(exitHitbox)) {
                showExitSign = true;
            }

            // RETURN TO ROOM 1 (Walking Left)
            if (player.x <= -275) { 
                currentRoom = 1;
                player.x = 850; // Set this slightly lower than the Room 2 trigger
            }

            // RETURN TO TOWN (Walking Right)
            // Increased this to 1150 so it doesn't trigger accidentally
            if (player.x > 900) { 
                game.setScreen(new GameScreen(game, true, returnX, returnY));
            }
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
    }
}