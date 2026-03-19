import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WavReader {
    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
        File wavFile = new File("eeriemusic.wav");
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
        AudioFormat format = audioStream.getFormat();

        System.out.println("Channels: " + format.getChannels());
        System.out.println("Sample Rate: " + format.getSampleRate());
        System.out.println("Sample Size: " + format.getSampleSizeInBits() + " bits");
        System.out.println("Encoding: " + format.getEncoding());

        audioStream.close();
    }
}
