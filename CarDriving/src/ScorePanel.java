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

    public ScorePanel() {
        setLayout(new FlowLayout(FlowLayout.RIGHT));  // Align components to the right

        moneyLabel = new JLabel("Money: $0");
        scoreLabel = new JLabel("Score: 0");
        highScoreLabel = new JLabel("High Score: 0");

        add(scoreLabel);
        add(moneyLabel);
        add(highScoreLabel);

        random = new Random();
        
        setPreferredSize(new Dimension(800, 50));  // Set the preferred size for the panel
    }

    // Method to reset the score and money when a new game starts
    public void reset() {
        score = 0;
        money = 0;
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
}
