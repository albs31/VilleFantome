package apcs.VilleFantome;

import javax.swing.*;
import java.awt.*;

public class ImageButton {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Image Button");
            JPanel panel = new JPanel();

            // Load image and set as button
            ImageIcon icon = new ImageIcon("/Users/suhasr/Downloads/warningscreen.PNG");
            JButton button = new JButton(icon);

            // Click action
            button.addActionListener(e -> System.out.println("Button clicked!"));

            panel.add(button);
            frame.add(panel);
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
