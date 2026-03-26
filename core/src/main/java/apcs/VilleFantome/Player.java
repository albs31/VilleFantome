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
    private float drawWidth = 550.0f;  
    private float drawHeight = 550.0f; 
    
    private float speed = 220.0f; // Default speed
    private float animationTimer = 0.0f;
    private float frameDuration = 0.15f;
    private boolean moving = false;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

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

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * delta;
            animationTimer += delta;
            int frameIndex = (int)(animationTimer / frameDuration) % leftFrames.length;
            currentFrame = leftFrames[frameIndex];
            moving = true;
        } 
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * delta;
            animationTimer += delta;
            int frameIndex = (int)(animationTimer / frameDuration) % rightFrames.length;
            currentFrame = rightFrames[frameIndex];
            moving = true;
        }

        if (!moving) {
            currentFrame = playerIdle;
            animationTimer = 0;
        }
    }

    public void draw(SpriteBatch batch) {
        batch.draw(currentFrame, x, y, drawWidth, drawHeight);
    }

    public void dispose() {
        playerIdle.dispose();
        for (Texture t : leftFrames) t.dispose();
        for (Texture t : rightFrames) t.dispose();
    }
    
    public float getWidth() { return drawWidth; }
    public float getHeight() { return drawHeight; }

    public void setDrawSize(float width, float height) {
        this.drawWidth = width;
        this.drawHeight = height;
    }

    // NEW METHOD: Allows us to boost speed in the shop
    public void setSpeed(float newSpeed) {
        this.speed = newSpeed;
    }
}