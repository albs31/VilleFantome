package apcs.VilleFantome;

import com.badlogic.gdx.Game;

public class Main extends Game {

    private MusicPlayer player; // Defining it here so we can use it later

    @Override
    public void create() {
        // 1. Initialize and play the music
        player = new MusicPlayer();
        player.playMusic();

        // 2. Start the loading screen
        setScreen(new LoadingScreen()); 
    }

    @Override
    public void render() {
        super.render(); // This is required for screens to show up!
    }

    @Override
    public void dispose() {
        // Clean up everything when the game closes
        if (getScreen() != null) getScreen().dispose();
        if (player != null) player.stopMusic();
    }
}