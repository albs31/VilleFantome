package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PreviousRoomScreen implements Screen {
    private Main game;
    private SpriteBatch batch;
    private Texture roomPt1, roomPt2, exitSign;
    private Player player;
    private Stage stage;
    private Inventory inventory;

    private int currentRoom = 1; // Now starts in Room 1
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
        
        roomPt1 = new Texture("His_Previous_Room1.png");
        roomPt2 = new Texture("His_Previous_Room2.png");
        exitSign = new Texture("exitsign.png");

        // Spawn on the left side of Room 1
        player = new Player(20, -100); 
        player.setDrawSize(1000, 1000);
        player.setSpeed(335.0f); 

        playerBounds = new Rectangle();
        
        // Exit hitbox (to return to town) is now in Room 2 on the right side
        exitHitbox = new Rectangle(1000, 0, 280, 720); 
    }

    @Override
    public void render(float delta) {
        updateGame(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        
        // Draw backgrounds based on room state
        batch.draw(currentRoom == 1 ? roomPt1 : roomPt2, 0, 0, 1280, 720);
        
        player.draw(batch);

        // Show exit sign only in Room 2 when near the right edge
        if (currentRoom == 2 && showExitSign) {
            batch.draw(exitSign, 0, 0, 1280, 720); 
        }

        batch.end();

        inventory.handleInput();
        inventory.render(delta);
    }

    private void updateGame(float delta) {
        player.update(delta);
        playerBounds.set(player.x + 170, player.y + 40, 140, 260);

        showExitSign = false;

        if (currentRoom == 1) {
            // Room 1 Left boundary (Wall)
            if (player.x < -300) player.x = -300; 

            // TRANSITION: Go to Room 2 (Walking Right)
            if (player.x >= 1100) { 
                currentRoom = 2;
                player.x = -270; // Spawn on left of Room 2
            }
        } 
        else if (currentRoom == 2) {
            // Check for Exit Sign
            if (playerBounds.overlaps(exitHitbox)) {
                showExitSign = true;
            }

            // TRANSITION: Back to Room 1 (Walking Left)
            if (player.x <= -275) { 
                currentRoom = 1;
                player.x = 850; // Spawn back in Room 1
            }

            // RETURN TO TOWN (Walking Right)
            if (player.x > 900) { 
    game.setScreen(new GameScreen(game, true, returnX, returnY, 1));
}
        }
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        batch.dispose();
        roomPt1.dispose();
        roomPt2.dispose();
        exitSign.dispose();
        player.dispose();
        inventory.dispose();
        stage.dispose();
    }
}