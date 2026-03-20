package apcs.VilleFantome;

import com.badlogic.gdx.Game;

public class Main extends Game {
    private MusicPlayer player;

    @Override
    public void create() {
        // Initialize and play the music
        player = new MusicPlayer();
        player.playMusic();

        // Start the loading screen
        setScreen(new LoadingScreen(this)); 
    }

    @Override
    public void render() {
        // Essential: this allows the LoadingScreen to draw itself
        super.render(); 
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (player != null) player.stopMusic();
    }
}