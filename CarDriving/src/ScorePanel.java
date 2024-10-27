import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ScorePanel extends JPanel {

    private int money = 0;
    private int highScore = 0;
    private int score = 0;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;
    private JLabel levelLabel;
    private Font retroFont;
    private Color hizmetColor = new Color(155, 193, 51);

    public ScorePanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER));  // Align components to the right
        setBackground(hizmetColor);

        // Load the custom font
        loadCustomFont();

        // Initialize labels with the custom font
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(retroFont.deriveFont(Font.BOLD, 25));
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,100));
        scoreLabel.setForeground(Color.WHITE);

        highScoreLabel = new JLabel("High Score: 0");
        highScoreLabel.setFont(retroFont.deriveFont(Font.BOLD, 25));
        highScoreLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,100));
        highScoreLabel.setForeground(Color.WHITE);

        levelLabel = new JLabel("Level: 1");  // Initial level display
        levelLabel.setFont(retroFont.deriveFont(Font.BOLD, 25));
        levelLabel.setForeground(Color.WHITE);

        // Add labels to the panel
        add(scoreLabel);
        add(highScoreLabel);
        add(levelLabel);

        setPreferredSize(new Dimension(800, 50));  // Set the preferred size for the panel
    }

    // Load custom font from file or classpath
    private void loadCustomFont() {
        try {
            // Attempt to load the font from the classpath
            InputStream fontStream = getClass().getResourceAsStream("/retro_font.ttf");
            if (fontStream != null) {
                retroFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(25f);
                System.out.println("Custom font loaded successfully from resources!");
            } else {
                // If not found in resources, fallback to loading from a file path (update as needed)
                retroFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/retro_font.ttf")).deriveFont(25f);
                System.out.println("Custom font loaded successfully from file path!");
            }
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            System.out.println("Failed to load custom font. Using default font.");
            retroFont = new Font("SansSerif", Font.BOLD, 25);  // Fallback to default font if loading fails
        }
    }

    // Reset score and level
    public void reset() {
        score = 0;
        levelLabel.setText("Level: 1");
        updateLabels();
    }

    public void addRandomMoney() {
        Random random = new Random();
        int reward = random.nextInt(41) + 10;  // Random value between 10 and 50
        score += reward;
        updateLabels();
    }

    // Update high score if current score is greater
    private void updateLabels() {
        if (score > highScore) {
            highScore = score;
        }
        scoreLabel.setText("Score: " + score);
        highScoreLabel.setText("High Score: " + highScore);
    }

    // Method to update the level display
    public void updateLevel(int level) {
        levelLabel.setText("Level: " + level);
    }
}
