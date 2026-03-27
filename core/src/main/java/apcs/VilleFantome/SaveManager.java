package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {
    private static final String SAVE_FILE = "villefantome_save";

    public static void save(float playerX, float playerY, int currentArea, int dialogueIndex) {
        Preferences prefs = Gdx.app.getPreferences(SAVE_FILE);
        prefs.putFloat("playerX", playerX);
        prefs.putFloat("playerY", playerY);
        prefs.putInteger("currentArea", currentArea);
        prefs.putInteger("dialogueIndex", dialogueIndex);
        prefs.putBoolean("hasSave", true);
        prefs.flush(); // Actually writes to disk
    }

    public static boolean hasSave() {
        return Gdx.app.getPreferences(SAVE_FILE).getBoolean("hasSave", false);
    }

    public static float loadPlayerX() { return Gdx.app.getPreferences(SAVE_FILE).getFloat("playerX", 10); }
    public static float loadPlayerY() { return Gdx.app.getPreferences(SAVE_FILE).getFloat("playerY", 20); }
    public static int loadArea()      { return Gdx.app.getPreferences(SAVE_FILE).getInteger("currentArea", 1); }
    public static int loadDialogue()  { return Gdx.app.getPreferences(SAVE_FILE).getInteger("dialogueIndex", 0); }

    public static void deleteSave() {
        Preferences prefs = Gdx.app.getPreferences(SAVE_FILE);
        prefs.clear();
        prefs.flush();
    }
}