package apcs.VilleFantome;

import com.badlogic.gdx.Game;

public class Main extends Game {
    // Make this public so screens can reach it
    public MusicPlayer musicController; 

    @Override
    public void create() {
        musicController = new MusicPlayer();
        // Only start it once here!
        musicController.playMusic();

        setScreen(new LoadingScreen(this)); 
    }

    @Override
    public void render() {
        super.render(); 
    }

    @Override
    public void dispose() {
        super.dispose();
        if (musicController != null) musicController.stopMusic();
    }
}