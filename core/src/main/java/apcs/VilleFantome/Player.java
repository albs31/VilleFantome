package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    private Texture playerIdle;
    private Texture[] leftFrames;
    private Texture[] rightFrames;
    private Texture currentFrame;

    public float x, y;
    private float speed = 300.0f;
    private float animationTimer = 0.0f;
    private float frameDuration = 0.15f;
    
    // Track which way the player is facing to stay in that direction when idle
    private boolean facingRight = true;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        // Ensure these filenames match your assets folder exactly
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
        boolean moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * delta;
            animationTimer += delta;
            // Pick frame from left array
            int frameIndex = (int)(animationTimer / frameDuration) % leftFrames.length;
            currentFrame = leftFrames[frameIndex];
            moving = true;
            facingRight = false;
        } 
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * delta;
            animationTimer += delta;
            // Pick frame from right array
            int frameIndex = (int)(animationTimer / frameDuration) % rightFrames.length;
            currentFrame = rightFrames[frameIndex];
            moving = true;
            facingRight = true;
        }

        if (!moving) {
            // When stopped, use the idle texture
            currentFrame = playerIdle;
            // Optional: reset timer so the next walk starts at frame 1
            animationTimer = 0;
        }

        // Keep player inside screen bounds (1280 is width, 150 is approx player width)
        x = Math.max(0, Math.min(x, 1280 - 150)); 
    }

    public void draw(SpriteBatch batch) {
        // Draw the player. Ensure the width (550) and height (400) 
        // match the aspect ratio of your actual .png files
        batch.draw(currentFrame, x, y, 550, 400);
    }

    public void dispose() {
        playerIdle.dispose();
        for (Texture t : leftFrames) t.dispose();
        for (Texture t : rightFrames) t.dispose();
    }
}