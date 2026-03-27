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
    private Texture roomPt1, roomPt2, exitSign, pauseBg, resumeTex, quitTex;
    private Player player;
    private Stage stage;
    private Inventory inventory;
    private ImageButton resumeButton, quitButton;

    private enum State { RUNNING, PAUSED }
    private State state = State.RUNNING;

    private int currentRoom = 1;
    private Rectangle playerBounds, exitHitbox;
    private boolean showExitSign = false;
    
    private float returnX, returnY;

    public PreviousRoomScreen(Main game, float x, float y) {
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
        
        roomPt1 = new Texture("His_Previous_Room1.png");
        roomPt2 = new Texture("His_Previous_Room2.png");
        exitSign = new Texture("exitsign.png");
        pauseBg = new Texture("pause_screen.png");
        resumeTex = new Texture("resume_button.png");
        quitTex = new Texture("quitbutton.png");

        player = new Player(20, -100); 
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f); 

        playerBounds = new Rectangle();
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
               SaveManager.save(player.x, player.y, 1, 7, "previousroom");
game.setScreen(new LoadingScreen(game));
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
        
        batch.draw(currentRoom == 1 ? roomPt1 : roomPt2, 0, 0, 1280, 720);
        player.draw(batch);

        if (currentRoom == 2 && showExitSign) batch.draw(exitSign, 0, 0, 1280, 720);
        if (state == State.PAUSED) batch.draw(pauseBg, 0, 0, 1280, 720);

        batch.end();

        inventory.handleInput();
        inventory.render(delta);

        if (state == State.PAUSED) { stage.act(delta); stage.draw(); }
    }

    private void updateGame(float delta) {
        player.update(delta);
        playerBounds.set(player.x + 170, player.y + 40, 140, 260);

        showExitSign = false;

        if (currentRoom == 1) {
            if (player.x < -300) player.x = -300; 
            if (player.x >= 1100) { 
                currentRoom = 2;
                player.x = -270;
            }
        } else if (currentRoom == 2) {
            if (playerBounds.overlaps(exitHitbox)) showExitSign = true;
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
        roomPt1.dispose(); roomPt2.dispose(); exitSign.dispose();
        pauseBg.dispose(); resumeTex.dispose(); quitTex.dispose();
    }
}