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

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        playerIdle = new Texture("standing_still.png");
        leftFrames = new Texture[]{new Texture("left(1).png"), new Texture("left(2).png"), new Texture("left(3).png")};
        rightFrames = new Texture[]{new Texture("right(1).png"), new Texture("right(2).png"), new Texture("right(3).png")};
        currentFrame = playerIdle;
    }

    public void update(float delta) {
        boolean moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * delta;
            animationTimer += delta;
            currentFrame = leftFrames[(int)(animationTimer / frameDuration) % 3];
            moving = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * delta;
            animationTimer += delta;
            currentFrame = rightFrames[(int)(animationTimer / frameDuration) % 3];
            moving = true;
        }

        if (!moving) {
            currentFrame = playerIdle;
            animationTimer = 0;
        }
    }

    public void draw(SpriteBatch batch) {
        batch.draw(currentFrame, x, y, 550, 400);
    }

    public void dispose() {
        playerIdle.dispose();
        for (Texture t : leftFrames) t.dispose();
        for (Texture t : rightFrames) t.dispose();
    }
}