import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// The main game frame class
public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private ScorePanel scorePanel;
    private boolean gameStarted = false;

    /**
     * Intro from start screen to game itself.
     */
    public GameFrame() {
        super("Hizmet Delivery Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Closes when press x in right corner
        setLayout(new BorderLayout());
        setResizable(false); // Stops player from resizing the window

        scorePanel = new ScorePanel();
        add(scorePanel, BorderLayout.NORTH);

        gamePanel = new GamePanel(scorePanel);
        add(gamePanel, BorderLayout.CENTER);


        // Ensure the game panel gets focus after making the window visible
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());

        // Add key listener for vehicle movement
        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameStarted) {
                    if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                        gamePanel.moveVehicle(e.getKeyCode());
                    }
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        gamePanel.setMovingLeft(true);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        gamePanel.setMovingRight(true);
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (gameStarted) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        gamePanel.setMovingLeft(false);
                    }
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        gamePanel.setMovingRight(false);
                    }
                }
            }
        });

        gamePanel.setFocusable(true);  // Ensure the panel is focusable

        // Make the window visible to the player
        setVisible(true);

        // Ensure panel has focus once the window becomes visible
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    // Method to handle game start
    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        if (started) {
            scorePanel.reset();  // Reset the score when the game starts
        }
    }

    public ScorePanel getScorePanel() {
        return scorePanel;
    }

    public static void main(String[] args) {
        new GameFrame();
    }
}