package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DialogueManager {

    public interface OnDialogueComplete {
        void onComplete();
    }

    private Texture[] pages;
    private float[] durations;
    private int currentIndex = 0;
    private float timer = 0f;
    private boolean active = false;
    private boolean waitingForInput = false;
    private OnDialogueComplete callback;

    /**
     * @param pageNames  texture file names in order
     * @param durations  seconds per page. 0f = wait for E key or click to advance
     * @param callback   runs when sequence finishes — use this to trigger next event
     */
    public DialogueManager(String[] pageNames, float[] durations, OnDialogueComplete callback) {
        this.pages = new Texture[pageNames.length];
        for (int i = 0; i < pageNames.length; i++) {
            this.pages[i] = new Texture(pageNames[i]);
        }
        this.durations = durations;
        this.callback = callback;
    }

    // starts or restarts this dialogue sequence from the beginning 
    public void start() {
        currentIndex = 0;
        timer = 0f;
        active = true;
        waitingForInput = durations[0] == 0f;
    }

    // true while dialogue is showing, use to block player movement
    public boolean isActive() {
        return active;
    }

    public void update(float delta) {
        if (!active) return;

        if (waitingForInput) {
            // Advance forward
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || 
                Gdx.input.isKeyJustPressed(Input.Keys.E) || 
                Gdx.input.justTouched()) {
                advance();
            }
            // Go back
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                goBack();
            }
        } else {
            timer += delta;
            if (timer >= durations[currentIndex]) {
                advance();
            }
        }
    }

    private void advance() {
        currentIndex++;
        timer = 0f;
        if (currentIndex >= pages.length) {
            active = false;
            if (callback != null) callback.onComplete();
        } else {
            waitingForInput = durations[currentIndex] == 0f;
        }
    }

    private void goBack() {
        if (currentIndex > 0) {
            currentIndex--;
            timer = 0f;
            waitingForInput = durations[currentIndex] == 0f;
        }
    }

    // call inside of batch.begin() and batch.end() block, renders on top of everything 
    public void render(SpriteBatch batch) {
        if (!active) return;
        batch.draw(pages[currentIndex], 0, 0, 1280, 720);
    }

    public void dispose() {
        if (pages != null) {
            for (Texture t : pages) {
                if (t != null) t.dispose();
            }
        }
    }
}
