import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ScorePanel extends JPanel {

    private int money = 0;
    private int highScore = 0;
    private int score = 0;
    private JLabel moneyLabel;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;
    private Random random;
    private JLabel levelLabel;

    public ScorePanel() {
        setLayout(new FlowLayout(FlowLayout.RIGHT));  // Align components to the right
    
        moneyLabel = new JLabel("Money: $0");
        scoreLabel = new JLabel("Score: 0");
        highScoreLabel = new JLabel("High Score: 0");
        levelLabel = new JLabel("Level: 1");  // Initial level display
    
        add(scoreLabel);
        add(moneyLabel);
        add(highScoreLabel);
        add(levelLabel);  // Add the level label to the panel
    
        setPreferredSize(new Dimension(800, 50));  // Set the preferred size for the panel
    }

    // Method to reset the score and money when a new game starts
    public void reset() {
        score = 0;
        money = 0;
        updateLabels();
    }

    public void addRandomMoney() {
        Random random = new Random();
        int reward = random.nextInt(41) + 10;  // Random value between 10 and 50
        money += reward;
        updateLabels();
    }
    
    

    // Method to increase the score after a successful delivery
    public void successfulDelivery() {
        int reward = random.nextInt(50) + 50;  // Random reward between 50 and 100
        score += reward;
        money += reward;
        updateLabels();
    }

    // Method to decrease score when penalty occurs
    public void missedDelivery() {
        int penalty = random.nextInt(20) + 10;  // Random penalty between 10 and 30
        if (score > 0) {
            score = Math.max(0, score - penalty);  // Prevent score from going negative
        }
        updateLabels();
    }

    // Update the high score if needed and refresh the labels
    private void updateLabels() {
        if (score > highScore) {
            highScore = score;  // Update high score if current score is greater
        }

        moneyLabel.setText("Money: $" + money);
        scoreLabel.setText("Score: " + score);
        highScoreLabel.setText("High Score: " + highScore);
    }

    // Add a method to update the level display
    public void updateLevel(int level) {
        levelLabel.setText("Level: " + level);  // Update the level label
    }
}