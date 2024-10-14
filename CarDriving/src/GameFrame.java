import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


// The main game frame class
public class GameFrame extends JFrame {

    private GamePanel gamePanel;
    private ScorePanel scorePanel;
    private JButton startButton;
    private JButton instructionButton;
    private boolean gameStarted = false;

     /**
      * Intro from start screen to game itself.
      */
    public GameFrame(){
        super("Hizmet delivery game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Closes when press x in right corner
        setLayout(new BorderLayout());
        setResizable(false); // Stops player from resizing window
        
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        gamePanel.setFocusable(true);

        scorePanel = new ScorePanel();
        add(scorePanel,BorderLayout.NORTH);
       // startButton = new JButton("Start Game:");

       // instructionButton = new JButton("Instructions");

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameStarted) {
                    gamePanel.moveVehicle(e.getKeyCode());
                }
            }
        });

        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        setVisible(true); // Makes window visible to the player
        gamePanel.requestFocusInWindow();
        //Ensure panel has focus for key events
        //such as left/right and up/down movements
    }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        if (started) {
            scorePanel.reset();
        }
    }
    
    public ScorePanel getScorePanel(){
        return scorePanel;
    }
    

    public static void main(String[] args){
        new GameFrame();
    }
}
