package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.ArrayList;

public class Inventory {
    private boolean isOpen = false;
    private boolean viewingEvidence = false;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Stage stage;

    private int selectedIndex = 0; // which item is highlighted

    // Static evidence list: name + texture filename
    private static ArrayList<String> collectedItems = new ArrayList<>();
    private static ArrayList<String> collectedTextures = new ArrayList<>();

    // The texture currently being viewed
    private com.badlogic.gdx.graphics.Texture evidenceTexture = null;

    public Inventory() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.0f);
    }

    // Call this from any screen to add evidence
    // itemName = display name, textureName = png filename
    public static void addItem(String itemName, String textureName) {
        if (!collectedItems.contains(itemName)) {
            collectedItems.add(itemName);
            collectedTextures.add(textureName);
        }
    }

    public static void clearItems() {
        collectedItems.clear();
        collectedTextures.clear();
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            if (viewingEvidence) {
                // close evidence view, go back to list
                viewingEvidence = false;
                if (evidenceTexture != null) {
                    evidenceTexture.dispose();
                    evidenceTexture = null;
                }
            } else {
                isOpen = !isOpen;
                selectedIndex = 0;
            }
        }

        if (!isOpen || viewingEvidence) return;

        // Scroll up/down through list
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = Math.min(collectedItems.size() - 1, selectedIndex + 1);
        }

        // Press E to open selected evidence
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !collectedItems.isEmpty()) {
            String texName = collectedTextures.get(selectedIndex);
            evidenceTexture = new com.badlogic.gdx.graphics.Texture(texName);
            viewingEvidence = true;
        }
    }

    public void render(float delta) {
        if (!isOpen) return;

        // Draw dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        shapeRenderer.rect(0, 0, 1280, 720);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();

        if (viewingEvidence && evidenceTexture != null) {
            // Show fullscreen evidence image
            batch.draw(evidenceTexture, 0, 0, 1280, 720);
            font.setColor(Color.GRAY);
            font.draw(batch, "Press Q to go back", 490, 40);
        } else {
            // Show evidence list
            font.setColor(Color.WHITE);
            font.draw(batch, "== EVIDENCE ==", 480, 650);

            if (collectedItems.isEmpty()) {
                font.setColor(Color.GRAY);
                font.draw(batch, "No evidence collected yet.", 380, 580);
            } else {
                float yPos = 580;
                for (int i = 0; i < collectedItems.size(); i++) {
                    if (i == selectedIndex) {
                        font.setColor(Color.YELLOW); // highlight selected
                    } else {
                        font.setColor(Color.WHITE);
                    }
                    font.draw(batch, "- " + collectedItems.get(i), 400, yPos);
                    yPos -= 50;
                }
                font.setColor(Color.GRAY);
                font.draw(batch, "UP/DOWN to scroll   E to view   Q to close", 300, 60);
            }
        }

        batch.end();
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        stage.dispose();
        if (evidenceTexture != null) evidenceTexture.dispose();
    }

    public boolean isOpen() {
        return isOpen;
    }
}