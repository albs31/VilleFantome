package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {
    private static final String SAVE_FILE = "villefantome_save";

    public static void save(float playerX, float playerY, int currentArea, int dialogueIndex, String roomScreen) {
        Preferences prefs = Gdx.app.getPreferences(SAVE_FILE);
        prefs.putFloat("playerX", playerX);
        prefs.putFloat("playerY", playerY);
        prefs.putInteger("currentArea", currentArea);
        prefs.putInteger("dialogueIndex", dialogueIndex);
        prefs.putString("roomScreen", roomScreen);
        prefs.putBoolean("hasSave", true);

        // Save inventory — store as comma-separated strings
        java.util.ArrayList<String> items = Inventory.getCollectedItems();
        java.util.ArrayList<String> textures = Inventory.getCollectedTextures();
        prefs.putInteger("inventoryCount", items.size());
        for (int i = 0; i < items.size(); i++) {
            prefs.putString("item_" + i, items.get(i));
            prefs.putString("tex_" + i, textures.get(i));
        }

        prefs.flush();
    }

    public static void loadInventory() {
        Preferences prefs = Gdx.app.getPreferences(SAVE_FILE);
        Inventory.clearItems();
        int count = prefs.getInteger("inventoryCount", 0);
        for (int i = 0; i < count; i++) {
            String item = prefs.getString("item_" + i, "");
            String tex = prefs.getString("tex_" + i, "");
            if (!item.isEmpty()) Inventory.addItem(item, tex);
        }
    }

    public static boolean hasSave() { return Gdx.app.getPreferences(SAVE_FILE).getBoolean("hasSave", false); }
    public static float loadPlayerX()     { return Gdx.app.getPreferences(SAVE_FILE).getFloat("playerX", 10); }
    public static float loadPlayerY()     { return Gdx.app.getPreferences(SAVE_FILE).getFloat("playerY", 20); }
    public static int   loadArea()        { return Gdx.app.getPreferences(SAVE_FILE).getInteger("currentArea", 1); }
    public static int   loadDialogue()    { return Gdx.app.getPreferences(SAVE_FILE).getInteger("dialogueIndex", 7); }
    public static String loadRoomScreen() { return Gdx.app.getPreferences(SAVE_FILE).getString("roomScreen", "town"); }

    public static void deleteSave() {
        Preferences prefs = Gdx.app.getPreferences(SAVE_FILE);
        prefs.clear();
        prefs.flush();
    }
}