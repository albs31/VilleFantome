package apcs.VilleFantome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicPlayer { 

    private Music music;

    public void playMusic() {
        // LibGDX looks in your 'assets' folder by default
        music = Gdx.audio.newMusic(Gdx.files.internal("eeriemusic.mp3"));
        
        music.setLooping(true);
        music.setVolume(0.5f); // Set volume between 0.0 and 1.0
        music.play();
    }

    public void stopMusic() {
        if (music != null) {
            music.stop();
            music.dispose(); // Important: Frees up memory
        }
    }

    public void pauseMusic() {
        if (music != null && music.isPlaying()) {
            music.pause();
        }
    }

    public void resumeMusic() {
        if (music != null && !music.isPlaying()) {
            music.play();
        }
    }
}