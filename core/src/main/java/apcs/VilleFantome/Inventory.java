package apcs.VilleFantome;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Inventory {
    private boolean isOpen = false;
    private boolean viewingEvidence = false;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Stage stage;

    private int selectedIndex = 0;

    private static ArrayList<String> collectedItems = new ArrayList<>();
    private static ArrayList<String> collectedTextures = new ArrayList<>();

    private com.badlogic.gdx.graphics.Texture evidenceTexture = null;

    private static final String SAVE_FILE = "villefantome_save";

    public Inventory() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        stage = new Stage(new FitViewport(1280, 720));
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2.0f);
    }

    public static void addItem(String itemName, String textureName) {
        if (!collectedItems.contains(itemName)) {
            collectedItems.add(itemName);
            collectedTextures.add(textureName);
            autoSave();
        }
    }

    // ← KEY METHOD: lets any screen check if item was already picked up
    public static boolean hasItem(String itemName) {
        return collectedItems.contains(itemName);
    }

    private static void autoSave() {
        Preferences prefs = Gdx.app.getPreferences(SAVE_FILE);
        prefs.putBoolean("hasSave", true);
        prefs.putInteger("inventoryCount", collectedItems.size());
        for (int i = 0; i < collectedItems.size(); i++) {
            prefs.putString("item_" + i, collectedItems.get(i));
            prefs.putString("tex_" + i, collectedTextures.get(i));
        }
        prefs.flush();
    }

    public static void clearItems() {
        collectedItems.clear();
        collectedTextures.clear();
    }

    public static ArrayList<String> getCollectedItems() {
        return collectedItems;
    }

    public static ArrayList<String> getCollectedTextures() {
        return collectedTextures;
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            if (viewingEvidence) {
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = Math.min(collectedItems.size() - 1, selectedIndex + 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !collectedItems.isEmpty()) {
            String texName = collectedTextures.get(selectedIndex);
            evidenceTexture = new com.badlogic.gdx.graphics.Texture(texName);
            viewingEvidence = true;
        }
    }

    public void render(float delta) {
        if (!isOpen) return;

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
            batch.draw(evidenceTexture, 0, 0, 1280, 720);
            font.setColor(Color.GRAY);
            font.draw(batch, "Press Q to go back", 490, 40);
        } else {
            font.setColor(Color.WHITE);
            font.draw(batch, "COLLECTED EVIDENCE", 430, 650);

            if (collectedItems.isEmpty()) {
                font.setColor(Color.WHITE);
                GlyphLayout layout = new GlyphLayout(font, "No evidence has been collected, continue to explore the town.");
                font.draw(batch, layout, (1280 - layout.width) / 2, 580);
            } else {
                float yPos = 580;
                for (int i = 0; i < collectedItems.size(); i++) {
                    font.setColor(i == selectedIndex ? Color.YELLOW : Color.WHITE);
                    font.draw(batch, "- " + collectedItems.get(i), 400, yPos);
                    yPos -= 50;
                }
                font.setColor(Color.GRAY);
                font.draw(batch, "Arrow keys to scroll  //  E to view  //  Q to close", 270, 60);
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