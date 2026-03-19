package apcs.VilleFantome;

import com.badlogic.gdx.Game;

public class Main extends Game {

    private MusicPlayer player; 

    @Override
    public void create() {
        // LibGDX is now ready! We can load files here.
        player = new MusicPlayer();
        player.playMusic();

        // Start your loading screen
        setScreen(new LoadingScreen()); 
    }

    @Override
    public void render() {
        super.render(); // Essential for the screen to show up
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (player != null) player.stopMusic();
    }
}