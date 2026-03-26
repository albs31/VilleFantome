package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    // 1. TEXTURES
    private Texture playerIdle;
    private Texture[] leftFrames;
    private Texture[] rightFrames;
    private Texture currentFrame;

    // 2. POSITION & SIZE (Change these to make him bigger!)
    public float x, y;
    private float drawWidth = 550.0f;  // Try a large number first to see the change
    private float drawHeight = 550.0f; // Try a large number first to see the change
    
    // 3. MOVEMENT & ANIMATION
    private float speed = 300.0f;
    private float animationTimer = 0.0f;
    private float frameDuration = 0.15f;
    private boolean moving = false;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        // Load Assets
        playerIdle = new Texture("standing_still.png");
        leftFrames = new Texture[]{
            new Texture("left(1).png"), 
            new Texture("left(2).png"), 
            new Texture("left(3).png")
        };
        rightFrames = new Texture[]{
            new Texture("right(1).png"), 
            new Texture("right(2).png"), 
            new Texture("right(3).png")
        };
        
        currentFrame = playerIdle;
    }

    public void update(float delta) {
        moving = false;

        // LEFT Movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * delta;
            animationTimer += delta;
            int frameIndex = (int)(animationTimer / frameDuration) % leftFrames.length;
            currentFrame = leftFrames[frameIndex];
            moving = true;
        } 
        // RIGHT Movement
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * delta;
            animationTimer += delta;
            int frameIndex = (int)(animationTimer / frameDuration) % rightFrames.length;
            currentFrame = rightFrames[frameIndex];
            moving = true;
        }

        // IDLE Logic
        if (!moving) {
            currentFrame = playerIdle;
            animationTimer = 0;
        }
    }

    public void draw(SpriteBatch batch) {
        // This uses the variables from the top of the class
        batch.draw(currentFrame, x, y, drawWidth, drawHeight);
    }

    public void dispose() {
        playerIdle.dispose();
        for (Texture t : leftFrames) t.dispose();
        for (Texture t : rightFrames) t.dispose();
    }
    
    // Getters for width/height in case GameScreen needs them
    public float getWidth() { return drawWidth; }
    public float getHeight() { return drawHeight; }

    // Lets other screens change player size without changing this file again
    public void setDrawSize(float width, float height) {
        this.drawWidth = width;
        this.drawHeight = height;
    }
}
